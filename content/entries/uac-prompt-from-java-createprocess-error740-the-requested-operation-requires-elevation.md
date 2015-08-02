I just finished an absolutely monstrous project at work that involved quite a bit of Java and a little Visual C++.  The latter part of this project involved writing some VC++ that interactively upgraded a piece of Java based software installed on a PC.  This sounds relatively mundane, but frankly, it wasn't.  I ended up spending almost of week of engineering effort, writing code that gracefully understands how to deal with [Window's User Account Control (UAC)](http://en.wikipedia.org/wiki/User_Account_Control).  If you're not familiar with UAC, it's that incredibly annoying security mechanism in Vista, and Win7, that prompts the user for confirmation if a piece of software attempts to make any changes to a protected location on the computer.  I appreciate what Microsoft was trying to accomplish with UAC, but it couldn't be more painful for a developer to work with.  Not to mention I couldn't find any decent documentation from Microsoft that discussed how to properly integrate your software with UAC.  I found a lot of marketing type documents and other nonsense through MSDN, and eventually gave up in disgust.  All I wanted was a simple document, ideally one titled "this is how to open a UAC prompt in your application."

In the end, I figured out how to deal with UAC by studying the source code of the **Mozilla Firefox Updater** (knowing that Firefox is open-source, so I can look at its code, and it always seems to update itself just fine on my development Win 7 and Vista boxes).  This post is an attempt to document how I built an application that understands, and gracefully handles UAC.

So, you need to modify a file or directory in a protected location on the file system, eh?  Like `Program Files`?  Or, maybe you need to register a DLL or other library as an Administrator?  In Vista, and Windows 7, your application can't do any of these things without elevating itself to an Administrator, or privileged user.  Your application running as a normal user, must programmatically elevate itself to an Administrator before it has permission to run these privileged operations.  Your application needs to trigger Windows to display a UAC prompt.  Welcome to the painful world of Windows User Account Control.

Here are several important points to remember about UAC (things I learned the hard way):

* Unprivileged applications running as normal users, cannot simply fork another process to trigger a UAC prompt.  Even if the `requestedExecutionLevel` of your application manifest is `requireAdministrator`.  Using `_spawnv()`, `exec()`, etc. with a binary that has `requireAdministrator` set in its manifest will NOT work.  For example, a Java app running as a normal user cannot spawn a process with privileged access; even in the process Java is trying to spawn has the correct `requireAdministrator manifest property.
* A unprivileged application can only trigger a UAC prompt using the [ShellExecute](http://msdn.microsoft.com/en-us/library/bb762153%28VS.85%29.aspx) or [ShellExecuteEx](http://msdn.microsoft.com/en-us/library/bb762154%28VS.85%29.aspx) shell functions, provided via `shell32.lib`.
* Setting `requireAdministrator` in your application manifest only appears to open a UAC prompt when a user double clicks on your executable in Windows Explorer.
* If you are writing a Java application that needs to do something privileged on the computer, you should know you can't directly do it in Java.  If you follow good software engineering practices, your Java application (the JRE) will run as a normal user, the user that started the application.  So, when you need to do something privileged your Java application must spawn another helper application (that you have to write) that understands UAC and prompts the user if necessary.  If the user accepts the UAC prompt from your native helper application, then it can do what it needs to do in a privileged mode (e.g, moving files around under `Program Files/`, registering a DLL, etc.).

### The Application Manifest

Several resources claim you can trigger a UAC by simply inserting an [application manifest](https://onyx.koli.ch/get/3101/uac-manifest-xml.txt) into the assembly of your application binary.  An application manifest that triggers a UAC looks something like this:

```xml
<assembly xmlns="urn:schemas-microsoft-com:asm.v1" manifestVersion="1.0">
  <assemblyIdentity version="1.0.0.0" processorArchitecture="X86"
       name="yourapp.exe" type="win32">
  </assemblyIdentity>
  <description>Some Application Description</description>
  <dependency>
    <dependentAssembly>
      <assemblyIdentity type="win32"
         name="Microsoft.Windows.Common-Controls"
         version="6.0.0.0" processorArchitecture="*"
         publicKeyToken="6595b64144ccf1df" language="*" />
    </dependentAssembly>
  </dependency>
  <ms_asmv3:trustInfo xmlns:ms_asmv3="urn:schemas-microsoft-com:asm.v3">
    <ms_asmv3:security>
      <ms_asmv3:requestedPrivileges>
        <ms_asmv3:requestedExecutionLevel level="requireAdministrator"
            uiAccess="false">
        </ms_asmv3:requestedExecutionLevel>
      </ms_asmv3:requestedPrivileges>
    </ms_asmv3:security>
  </ms_asmv3:trustInfo>
</assembly>
```

Note the `level=requireAdministrator` attribute in this XML that I alluded to earlier.  This works, but only when the user double-clicks your executable, or launches it from the Start Menu.  This is hardly sufficient for an application that needs to become an Administrator when updating itself.  You'll need something more.

### Basic UAC Flow

Ok, so before you dig into it, I thought it might be helpful to explain the basic flow of a UAC aware application and how everything fits together.  Normally, your application runs as an unprivileged user.  But, sometimes it needs to be an Administrator (to do whatever).  So, here's the basic idea, in **pseudo code**:

```cpp
int main (int argc, char **argv) {

  HRESULT operation = tryToDoSomethingPrivileged();

  if (operation == ACCESS_DENIED && !alreadyElevated) {

    // Spawn a copy of ourselves, via ShellExecuteEx().
    // The "runas" verb is important because that's what
    // internally triggers Windows to open up a UAC prompt.
    HANDLE child = ShellExecuteEx(argc, argv, "runas");

    if (child) {
      // User accepted UAC prompt (gave permission).
      // The unprivileged parent should wait for
      // the privileged child to finish.
      WaitForSingleObject(child, INFINITE);
      CloseHandle(pid);
    }
    else {
      // User rejected UAC prompt.
      return FAILURE;
    }

    return SUCCESS;

  }

  return SUCCESS;

}
```

On Windows XP, or other versions of Windows with UAC disabled, note that the user will simply run right through the code fragment without any prompt.  However, if UAC is enabled, the privileged operation will be rejected meaning the application needs to spawn a copy of itself using `ShellExecuteEx`.

Here's a quick code snippet showing the usage of `ShellExecuteEx` to open a UAC prompt:

```cpp
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
```

Note the `runas` `lpVerb` in the [SHELLEXECUTEINFO struct](http://msdn.microsoft.com/en-us/library/bb759784%28VS.85%29.aspx) &mdash; this is the verb that triggers windows to "shell execute" your application via UAC.

### An Example

I whipped up a quick yet complete UAC example in Visual C++.  You can download the entire [VC++ project here](https://github.com/markkolich/blog/blob/master/content/static/entries/uac-prompt-from-java-createprocess-error740-the-requested-operation-requires-elevation/uac-example.zip?raw=true).  Or, download just the [pre-compiled release binary](https://github.com/markkolich/blog/blob/master/content/static/entries/uac-prompt-from-java-createprocess-error740-the-requested-operation-requires-elevation/uac-example-bin.zip?raw=true) if you want to experiment.  Or, perhaps you'd prefer just the CPP source code.  This VC++ example code was built using Visual C++ 2008 Express Edition.

The UAC demo, aptly named `uac-example`, should be run from a command prompt with a single argument:

```
C:\> uac-example.exe <working directory>
```

My UAC example works by attempting to create an empty file in the working directory specified by the command line argument.  If the working directory happens to be a Windows protected location on the file system, like `C:\Program Files`, the example app will initially fail to create this empty file.  In that case, it will re-spawn a copy of itself via a UAC prompt and try again as an Administrator.  If the app successfully created the file when elevated, you will see the following success message:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/uac-prompt-from-java-createprocess-error740-the-requested-operation-requires-elevation/worked-with-uac.png">

If you reject the UAC prompt (clicked Deny), you'll see this:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/uac-prompt-from-java-createprocess-error740-the-requested-operation-requires-elevation/rejected-uac-prompt.png">

If your working directory isn't a Windows protected directory, like `%APPDATA%` or your Desktop, you'll immediately see this without a UAC prompt:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/uac-prompt-from-java-createprocess-error740-the-requested-operation-requires-elevation/worked-no-uac-req.png">

Here are some example working directories you might like to try:

```
C:\> uac-example.exe "C:\Program Files"
C:\> uac-example.exe "%APPDATA%"
C:\> uac-example.exe .
```

Please note that if you're using a UAC capable computer, but UAC is turned off, no matter what directory you give to my example app it will always say "Worked (no UAC required)!"  This is obviously because if UAC is turned off, Windows isn't actively protecting any locations on the file system, so any running process can pretty much do whatever it wants.

### Opening a UAC Prompt From Java

If you need to open a UAC prompt from Java, you should know that there is no way to do so without writing your own native app. Your Java code should call your UAC enabled native app to do "whatever it needs to do" in a privileged mode.  Here's an example using my uac-example app with Java's ProcessBuilder:

```java
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UACTester {

  private static final String UAC_EXAMPLE_EXE = "uac-example.exe";

  public static void main(String[] args) {

    final File uacExample = new File(UAC_EXAMPLE_EXE);
    File workingDir;

    try {
      workingDir = new File(args[0]);
    } catch ( Exception e ) {
      workingDir = new File(".");
    }

    try {
      // Build the command list to be given to the ProcessBuilder
      final List<String> cmdArgs = new ArrayList<String>();
      cmdArgs.add(uacExample.getAbsolutePath());
      cmdArgs.add(workingDir.getAbsolutePath());

      // Create a process, and start it.
      final ProcessBuilder p = new ProcessBuilder(cmdArgs);
      p.directory(new File("."));
      p.start();
    } catch (Throwable t) {
      t.printStackTrace(System.out);
    }

  }

}
```

Note that if your Java app attempts to spawn a process as an Administrator, you'll most likely see an exception like this:

```
CreateProcess error=740, The requested operation requires elevation
...
```

If the argument given to my sample Java app is a UAC protected directory, the native executable it calls will need to open a UAC prompt.

Yay.

### Links and Other Resources

Here are a few links and other resources I gathered while writing this blog post and integrating UAC support into a project at work:

* [How to Embed a Manifest into an Assembly in Visual Studio](http://blogs.msdn.com/cheller/archive/2006/08/24/how-to-embed-a-manifest-in-an-assembly-let-me-count-the-ways.aspx)
* [MSDN: Designing Apps that Require Administrator Privileges](http://msdn.microsoft.com/en-us/magazine/cc163486.aspx#S4)
* [Getting Vista UAC elevation to work for web deployed ClickOnce applications](http://www.geektieguy.com/2007/08/25/getting-vista-uac-elevation-to-work-for-web-deployed-clickonce-applications/)
* [Onyx: UAC Resources (my personal UAC resources collection)](https://onyx.koli.ch/x2e3)

Cheers.

<!--- tags: windows, security -->