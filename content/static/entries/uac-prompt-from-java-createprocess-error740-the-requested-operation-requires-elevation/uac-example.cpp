/**
 * Copyright (c) 2009 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
*/

#include "stdafx.h"

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <shellapi.h>
#include <process.h>

#include "uac-example.h"

int WINAPI WinMain(HINSTANCE inst, HINSTANCE prevInst,
		LPSTR cmdLine, int nCmdShow) {

	LPWSTR *argv;
	int argc = 0;

	HRESULT ret = SUCCESS;

	WCHAR imagePath[MAXPATHLEN]; // Full path to this updater executable
	WCHAR workingDir[MAXPATHLEN]; // The working directory of this executable.

	WCHAR uacDir[MAXPATHLEN]; // Where we are going to attempt to create a file to see if we need UAC
	WCHAR uacRunningLockFilePath[MAXPATHLEN];
	HANDLE uacRunningLockFileHandle = INVALID_HANDLE_VALUE;

	WCHAR elevatedLockFilePath[MAXPATHLEN];
	HANDLE elevatedLockFileHandle = INVALID_HANDLE_VALUE;

	// Uncomment these lines if you want the demo
	// to open a console or attach itself to a running
	// console to see output from printf's and other
	// mechanisms writing to STDOUT.
	/*
	FILE *conin;
	FILE *conout;
	BOOL b = AttachConsole(ATTACH_PARENT_PROCESS);
	if (!b) {
		AllocConsole();
	}
	freopen_s(&conin, "conin$", "r", stdin);
	freopen_s(&conout, "conout$", "w", stdout);
	freopen_s(&conout, "conout$", "w", stderr);
	*/

	argv = CommandLineToArgvW(GetCommandLineW(), &argc);
	if(argv == NULL || argc < 2) {
		ERRORBOX("Missing required program arguments.\n\nUsage:\nuac-example.exe <working directory>");
		return FAILURE;
	}

	// Get the full path name to this executable itself.
	// Will need this if we have to spawn another copy
	// of ourselves to prompt the user with a UAC.
	GetModuleFileName(NULL, imagePath, MAXPATHLEN);
	argv[0] = imagePath;

	// The UAC directory, is the one and only argument into
	// this demo app.  This directory should be a UAC secured
	// location on the file system.  Usually, a good choice is
	// your "Program Files" directory. This is where this demo
	// will attempt to write a file to see if we need UAC.
	wcscpy_s((wchar_t *)uacDir, MAXPATHLEN, argv[1]);

	// Build the path to the running.lock file where the user
	// asked us to attempt to write a file.  If we can create this
	// file, then UAC is not required.  Otherwise, we need a UAC
	// prompt first to become an administrator.
	_snwprintf_s(uacRunningLockFilePath, MAXPATHLEN, MAXPATHLEN,
					_T("%s/") _T(RUNNING_LOCK_FILE), uacDir);

	// Setup the working directory, so we know where to
	// create our elevate.lock file.
	wcscpy_s(workingDir, MAXPATHLEN, imagePath);
	WCHAR *slash = wcsrchr(workingDir, '\\');
	wcscpy_s(slash, MAXPATHLEN, _T(""));

	// Build the path to the elevate.lock file which keeps track
	// if we are elevated or not.
	_snwprintf_s(elevatedLockFilePath, MAXPATHLEN, MAXPATHLEN,
						_T("%s/") _T(ELEVATE_LOCK_FILE), workingDir);

	// Try to create the running.lock file where we were asked to.
	// This will FAIL IF WE NEED UAC and we're trying to write the
	// file to a Windows protected directory.
	uacRunningLockFileHandle = CreateFileW(uacRunningLockFilePath,
								(GENERIC_READ | GENERIC_WRITE),
								// Prevents other processes from opening a file
								// or device if they request delete, read, or
								// write access.
								0,
								NULL,
								// Opens a file, always.
								// This "succeeds" even if the file exists AND
								// we don't have permission to write to it. This
								// does not succeed if the file does NOT exist
								// AND we don't have permission to create it.
								OPEN_ALWAYS,
								// Delete the file for us when we close the handle.
								FILE_FLAG_DELETE_ON_CLOSE,
								NULL);

	if (uacRunningLockFileHandle == INVALID_HANDLE_VALUE) {

		// Are we already elevated?
		// If the file exists and if I am already elevated.
		if (_waccess(elevatedLockFilePath, F_OK) == 0 &&
				_wremove(elevatedLockFilePath) != 0) {
			return FAILURE;
		}

		// Get the elevate lock accordingly; this is how we keep
		// track if we are elevated or not.
		elevatedLockFileHandle = CreateFileW(elevatedLockFilePath,
										(GENERIC_READ | GENERIC_WRITE),
										0,
										NULL,
										OPEN_ALWAYS,
										FILE_FLAG_DELETE_ON_CLOSE,
										NULL);

		// Did we get the lock file OK?
		if (elevatedLockFileHandle == INVALID_HANDLE_VALUE) {
			ERRORBOX("Unable to acquire the necessary permissions to run demo app.");
			return FAILURE;
		}

		// Respawn this binary using the "runas" verb on
		// Windows to trigger a UAC prompt.
		LPWSTR spawnCmdLine = BuildCommandLine(argc - 1, argv + 1);
		if (!spawnCmdLine) {
			CloseHandle(elevatedLockFileHandle);
			ERRORBOX("An error occured while respawning self.");
			return FAILURE;
		}
		
		SHELLEXECUTEINFO sinfo;
		memset(&sinfo, 0, sizeof(SHELLEXECUTEINFO));
		sinfo.cbSize       = sizeof(SHELLEXECUTEINFO);
		sinfo.fMask        = SEE_MASK_FLAG_DDEWAIT |
							   SEE_MASK_NOCLOSEPROCESS;
		sinfo.hwnd         = NULL;
		sinfo.lpFile       = argv[0];
		sinfo.lpParameters = spawnCmdLine;
		sinfo.lpVerb       = L"runas"; // <<-- this is what makes a UAC prompt show up
		sinfo.nShow        = SW_SHOWMAXIMIZED;

		// The only way to get a UAC prompt to show up
		// is by calling ShellExecuteEx() with the correct
		// SHELLEXECUTEINFO struct.  Non privlidged applications
		// cannot open/start a UAC prompt by simply spawning
		// a process that has the correct XML manifest.
		BOOL result = ShellExecuteEx(&sinfo);
		LocalFree(spawnCmdLine);
		
		if (result) {
			// The user accepted the UAC prompt.
			// If we spawned a new process successfully, then
			// wait here for it to finish then clean up.
			WaitForSingleObject(sinfo.hProcess, INFINITE);
			CloseHandle(sinfo.hProcess);
			ret = SUCCESS;
		} else {
			// User did not accept the UAC prompt.
			ret = FAILURE;
		}

		if (ret == FAILURE) {
			ERRORBOX("User rejected UAC prompt!");
		}
		else {
			SUCCESSBOX("Worked (with UAC)!");
		}

		return ret;

	}

	// If we are already elevated, then we exit here.
	// We know if we are elevated because we created
	// an elevate.lock file to indicate that we've
	// been elevated.  If we didn't exit here, then
	// the user would see the "Worked (no UAC required)!"
	// success dialog on every invocation.  But, instead,
	// we want to show the "Worked (with UAC)!" popup in
	// the parent process only.
	EXIT_IF_ELEVATED(elevatedLockFilePath,
				uacRunningLockFileHandle, SUCCESS);

	SUCCESSBOX("Worked (no UAC required)!");
	LocalFree(argv);
	
	return SUCCESS;

}

// ----------------------------------------------------------------------
// The following code was taken directly from the Mozilla Firefox Updater
// source tree, and slightly modified to support "Wide" strings in
// Visual C++.
// ----------------------------------------------------------------------

LPWSTR
BuildCommandLine(int argc, LPWSTR *argv) {
	
	int i;
	int len = 0;

	// The + 1 of the last argument handles the
	// allocation for null termination
	for (i = 0; i < argc; ++i) {
		len += ArgStrLen(argv[i]) + 1;
	}

	// Protect against callers that pass 0 arguments
	if (len == 0) {
		len = 1;
	}

	LPWSTR s = (LPWSTR)malloc(len * sizeof(LPWSTR));
	if (!s) {
		return NULL;
	}

	LPWSTR c = s;
	for (i = 0; i < argc; ++i) {
		c = ArgToString(c, argv[i]);
		if (i + 1 != argc) {
			*c = ' ';
			++c;
		}
	}

	*c = '\0';

	return s;

}

int
ArgStrLen(LPWSTR s) {

  int backslashes = 0;
  int i = wcslen(s);
  BOOL hasDoubleQuote = wcschr(s, L'"') != NULL;
  // Only add doublequotes if the string contains a space or a tab
  BOOL addDoubleQuotes = wcspbrk(s, L" \t") != NULL;

  if (addDoubleQuotes) {
    i += 2; // initial and final duoblequote
  }

  if (hasDoubleQuote) {
    while (*s) {
      if (*s == '\\') {
        ++backslashes;
      } else {
        if (*s == '"') {
          // Escape the doublequote and all backslashes preceding the doublequote
          i += backslashes + 1;
        }
        backslashes = 0;
      }

      ++s;
    }
  }

  return i;
}

LPWSTR
ArgToString(LPWSTR d, LPWSTR s) {

  int backslashes = 0;
  BOOL hasDoubleQuote = wcschr(s, L'"') != NULL;
  // Only add doublequotes if the string contains a space or a tab
  BOOL addDoubleQuotes = wcspbrk(s, L" \t") != NULL;

  if (addDoubleQuotes) {
    *d = '"'; // initial doublequote
    ++d;
  }

  if (hasDoubleQuote) {
    int i;
    while (*s) {
      if (*s == '\\') {
        ++backslashes;
      } else {
        if (*s == '"') {
		// Escape the doublequote and all backslashes\
		// preceding the doublequote
          for (i = 0; i <= backslashes; ++i) {
            *d = '\\';
            ++d;
          }
        }

        backslashes = 0;
      }
      *d = *s;
      ++d; ++s;
    }
  } else {
    wcscpy(d, s);
    d += wcslen(s);
  }

  if (addDoubleQuotes) {
    *d = '"'; // final doublequote
    ++d;
  }

  return d;
}