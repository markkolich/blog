I threw myself right into the bitness fire this afternoon, trying to figure out how to reliably determine if a Windows OS is 32-bit or native 64-bit.  I tried all sorts of things, everything from a VB Script, to a few tiny C++ programs built to issue WMI queries.

I also tried using WMI to read the `OSArchitecture` property:

```sql
SELECT * FROM Win32_OperatingSystem;
```

But that failed miserably on Windows XP.  As it turns out, the `OSArchitecture` property you can read via WMI wasn't added until Vista.  Nice.  So how do we check for 64-bit Windows XP?

After about an hour or so of searching, I stumbled across [this post](http://www.tech-archive.net/Archive/DotNet/microsoft.public.dotnet.languages.csharp/2007-06/msg03188.html) which described in reasonable detail what I needed to:

"In your code, you first need to check the size of IntPtr, if it returns 8 then you are running on a 64-bit OS. If it returns 4, you are running a 32 bit application, so now you need to know whether you are running natively or under WOW64. To get this information you will need to call kernel32.dll API "IsWow64Process" using PInvoke, this API returns a Boolean 'true' if you are running under WOW64, that means you are running 32 bit application on a 64-bit Windows system. Be careful however to check the OS version before calling this API, only XP SP2 and implements this one."

How could Microsoft make something that should be so easy, so complicated?  What a FAIL.

I then found another post, that offered up a nice little 32-bit C++ app one can compile and run to check the real, actual bitness of the OS.  For the most part, it does exactly what the first post said I needed to do, with the exception of checking the size of IntPtr's.  I cleaned it up a little bit, and **successfully compiled it on 32-bit Windows XP with Microsoft Visual C++ 2005 (Version 8.0.50)**.  This app checks the bitness of the OS by discovering if it's running under WOW64, or natively as a 32-bit app.  WOW64 stands for Windows-on-Windows, it's the 64-bit only kernel subsystem that lets 32-bit apps run on 64-bit Windows.  The bitness checker starts and then asks the Windows kernel if it's running under WOW64.  If it is running under WOW64, that clearly means it's a 32-bit app (as compiled) running on a 64-bit OS.  If it's not running in WOW64, then we're on a 32-bit OS:

```cpp
#include "stdafx.h"
#include <iostream>
#include "comutil.h"

#define RESPONSE_32_BIT "32"
#define RESPONSE_64_BIT "64"

using namespace std;

typedef BOOL (WINAPI *IW64PFP)(HANDLE, BOOL *);

int main(int argc, char **argv){

  BOOL res = FALSE;

  // When this application is compiled as a 32-bit app,
  // and run on a native 64-bit system, Windows will run
  // this application under WOW64.  WOW64 is the Windows-
  // on-Windows subsystem that lets native 32-bit applications
  // run in 64-bit land.  This calls the kernel32.dll
  // API to see if this process is running under WOW64.
  // If it is running under WOW64, then that clearly means
  // this 32-bit application is running on a 64-bit OS,
  // and IsWow64Process will return true.
  IW64PFP IW64P = (IW64PFP)GetProcAddress(
            GetModuleHandle(L"kernel32"), "IsWow64Process");

  if(IW64P != NULL){
    IW64P(GetCurrentProcess(), &res);
  }

  cout << ((res) ? RESPONSE_64_BIT : RESPONSE_32_BIT) << endl;

  return 0;

}
```

Will output `32` on 32-bit Windows and `64` on 64-bit Windows.

Download the [pre-built .exe and .cpp source here](static/entries/reliably-checking-os-bitness-32-or-64-bit-on-windows-with-a-tiny-c-app/bitness-checker.zip).

Tested and worked on:

* 32-bit Windows XP Professional
* 64-bit Windows XP Professional
* 32-bit Windows Vista Enterprise
* 64-bit Windows Vista Enterprise
* 32-Bit Windows 7 Home Premium
* 64-bit Windows 7 Home Premium
* 32-bit Windows Server 2003
* 64-bit Windows Server 2008

Enjoy.