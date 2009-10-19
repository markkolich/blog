If you ever use Java to check if a system is 32 or 64-bit, you should know that Java's `os.arch` system property returns the bitness of the JRE, not the OS itself.  Sites [like this are WRONG](http://www.roseindia.net/java/beginners/OSInformation.shtml) &mdash; any resource that claims Java's `os.arch` property returns the real "architecture of the OS" is lying.  Case in point, I recently ran this tiny program on a 64-bit Windows 7 machine, with a 32-bit JRE:

```java
import com.sun.servicetag.SystemEnvironment;

public class OSArchLies {

  public static void main(String[] args) {

    // Will say "x86" even on a 64-bit machine
    // using a 32-bit Java runtime
    SystemEnvironment env =
        SystemEnvironment.getSystemEnvironment();
    final String envArch = env.getOsArchitecture();

    // The os.arch property will also say "x86" on a
    // 64-bit machine using a 32-bit runtime
    final String propArch = System.getProperty("os.arch");

    System.out.println( "getOsArchitecture() says => " + envArch );
    System.out.println( "getProperty() says => " + propArch );

  }

}
```

The output from this tiny app on a 64-bit box:

```
#/> java OSArchLies
getOsArchitecture() says => x86
getProperty() says => x86
```

In this case, one would expect to see something like `x86_64` or `amd64` instead of just `x86`.  Bottom line, don't believe what you read about `os.arch` and other Java system properties.  They are usually properties of the JRE/JDK itself, and not necessarily the real properties of the underlying OS or architecture.  If you need to check if a system is actually 32 or 64-bit, you should look elsewhere in the system registry or [write your own native app and call it from Java](reliably-checking-os-bitness-32-or-64-bit-on-windows-with-a-tiny-c-app).