I've been playing around with a lot of installer type stuff recently.  I discovered that [Mozilla Firefox uses the 7zip SFX install launcher](http://howto.gumph.org/content/customize-firefox-installer/) kick off the Firefox installation process.  I started playing around with [7zip SFX, and realized that you can do some pretty cool stuff with it](http://7zsfx.solta.ru/en/).  In fact, I discovered that you can actually bundle a Java app and the Java Runtime Environment (JRE) into your own little 7zip SFX launcher.  Naturally, this means you can write a Java app and then let your users start it by double clicking a native Win32 .exe.  And best of all, because your launcher contains the Java Runtime Environment, the user does not have to have a JRE installed on their system to run your application!

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zip-sfx-extracting-shot-thumb-200x102.png">

The launcher extracts the JRE and your app to a temporary directory, then launches it using that freshly extracted JRE.

### Why Is This Useful

Java is fantastic for its write once, run anywhere methodology.  Only problem is, unlike a native Windows app, you need a JVM/JRE to run a Java application.  Most vendors who sell software written in Java tell their users or customers that they need to install a JRE first before they can run the app.  This makes sense, but it's a slight (err, huge) inconvenience; Sun's Java installer is bulky and often cumbersome.  Wouldn't it be nice if you could avoid that forced installation step, and simply ship a supported Java runtime with your Java application?  This way, the user simply double clicks an .exe, a launcher extracts a supported JRE, and starts.  In short, the user doesn't have to install a JRE at all, but rather the JRE they need is simply extracted to a temporary directory and your application starts using that freshly extracted JRE.  Further, when the user exits the application, the temporary JRE directory your app launcher created is automatically cleaned up, and all is well.

Not surprisingly, this is completely doable using 7zip SFX.  However, note that if you choose to ship the JRE with your launcher, you can expect your executable to be approximately **16MB larger** than it would be without the JRE.  IMHO, 16MB is a small price to pay for the added convenience of not having to install another piece of bloated software.  Plus you know that the JRE your launcher extracts and starts your application with fully supports your Java app; you don't have to worry about the Java updater updating the JRE on the user's system behind your back which might break your app.

### Getting Started

Before you start packaging up your app with 7zip, you'll probably want to [download my complete example pack](https://github.com/markkolich/blog/blob/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zipsfx-launch-java-example-pack.zip?raw=true).  This ZIP file contains everything you'll need to get started, including a ready to ship JRE (Java 6 Update 16) and an Ant build file.  Note that you do not need to install 7zip; I've packed the necessary 7zip.exe to create the archive with the [example pack](https://github.com/markkolich/blog/blob/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zipsfx-launch-java-example-pack.zip?raw=true).  However, if you want to install 7zip, can [download the installer here](http://www.7-zip.org/download.html) or [from my mirror on Onyx](https://onyx.koli.ch/x2v).  This sample pack is also an Eclipse project.  If you work out of Eclipse, you can import the .project inside of the example pack into your Eclipse IDE.

Or, if you want to see the 7zSD.sfx launcher in action, [download the pre-built demo launcher](https://github.com/markkolich/blog/blob/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zipsfx-launch-java-example-exe.zip?raw=true).

### Fundamentals

Here's how this all works.  7zip (and other ZIP installer type packages) provide SFX launchers.  These launchers are essentially native Windows executables that understand how to extract an archive to a temporary directory, and launch an application (usually another installer).  This is how the Mozilla Firefox installer works: when you launch the "installer" the extracting files dialog that opens is actually the 7zip SFX launcher extracting the real `setup.exe` to a temporary directory.  Once done, it starts setup.exe to complete the installation process.

In this case, the basic principle is the same, except I'm using the 7zip SFX launcher to extract my application and required JRE components to a temporary directory, and then start it.  Producing a native Windows SFX launcher is quite easy; you need to binary concatenate three files together: the SFX launcher, an `app.tag` configuration file, and a 7zip archive.  In Windows, using the copy command, this looks something like:

```
C:\> copy /b 7zSD.sfx + app.tag + app.7z start.exe
```

This produces `start.exe`, a portable native Windows app that contains everything your Java application needs to run in a single executable!  When run, `start.exe` will use `7zSD.sfx` to extract the contents of `app.7z` to a temporary directory, and launch whatever application you've defined in `app.tag`.

### My Sample Java App

My example Java app is very straightforward.  Yours will, of course, be more complicated.  My sample app simply opens a `JOptionPane` to display the current "working directory" (where the SFX launcher was started from) and the "temporary directory" (the temp directory where the SFX launcher extracted the JRE and application files to).

```java
package com.kolich.sevenzip.example;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class StartHere {

  private File workingDir_;
  private File tempDir_;

  public StartHere(File root, File temp){
    this.workingDir_ = root;
    this.tempDir_ = temp;
  }

  /**
   * The working directory, where the application was
   * started from.
   * @return
   */
  public File getWorkingDir(){
    return this.workingDir_;
  }

  /**
   * The temp directory, where the launcher extracted
   * your app and JRE to on the users' system.
   * @return
   */
  public File getTempDir(){
    return this.tempDir_;
  }

  public static void main(String[] args)
    throws Exception {

    File root;
    try {
      root = new File(args[0]);
    } catch ( Exception e ) {
      root = new File(".");
    }

    File temp;
    try {
      temp = new File(args[1]);
    } catch ( Exception e ) {
      temp = new File(".");
    }

    final StartHere sh = new StartHere(root, temp);
    Runnable worker = new Runnable() {
        public void run() {
          showMessageDialog(sh);
          System.exit(0);
        }
    };
    SwingUtilities.invokeLater(worker);

  }

  private static void showMessageDialog(StartHere sh) {
    try {
      JOptionPane.showMessageDialog(new JFrame(),
        "A java app launched by 7zip SFX!\n\n" +
        "My working directory is:\n" +
        sh.getWorkingDir().getCanonicalPath() +
        "\n\nAnd I've been extracted to temp directory:\n" +
        sh.getTempDir().getCanonicalPath() );
    } catch (IOException e) {
      e.printStackTrace( System.err );
    }

  }

}
```

Here's a screen shot:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zip-sfx-extracting-shot2-thumb-400x226.png">

The Ant build script in my example pack compiles this app and creates `app.jar`, a runnable JAR file.

### The App.tag Configuration File

I'm using the `7zSD.sfx` launcher by Oleg Scherbakov at http://7zsfx.solta.ru/en/.  There are a ton of configuration options as described on Oleg's web-site.  In the example, my `app.tag` configuration file is as follows:

```
;!@Install@!UTF-8!
Title="7ZIP Java Launcher Example"
ExtractDialogText="Extracting ..."
GUIFlags="32"
ExtractTitle="Extracting"
FinishMessage="Application stopped."
RunProgram="launcher\jre\bin\javaw.exe -jar launcher\app.jar \"%%S\" \"%%T\""
;!@InstallEnd@!
```

There's nothing too complicated about the configuration file.  In this example, I'm simply extracting the 7zip file included with the native SFX launcher and starting `launcher\jre\bin\javaw.exe`, which is the JRE packaged with the launcher (found under `launcher\jre` in the example pack).  The `%%S` property in the configuration file is the directory that contains the SFX executable (where the user started it from).  The `%%T` property is the temporary directory where the SFX launcher placed the extracted JRE and application files.  Note that when the Java application exits, the SFX launcher will automatically delete/cleanup this temporary directory.

This example simply asks `7zSD.sfx` to extract and then start `launcher\app.jar` using `launcher\jre\bin\javaw.exe`.

### The Ant Build File

My ant build file does a few things.  First, it compiles the Java app and packages it into a runnable JAR file.  From there, it uses 7zip to compress the JRE and the resulting JAR file into `app.7z`.  Finally, it uses Ant's concat task to binary concatenate `7zSD.sfx`, `app.tag` and the `app.7z` file together.  The result is `start.exe`, a native self-contained Windows executable that contains the JRE and Java app itself!

Note that the JRE is 7zip'ed inside of `app.7z`.  This is how the JRE is shipped/included with the launcher.

```xml
<project name="7zipexample" default="package.7zipexample">

  <property name="src.dir" location="${basedir}/src/com/kolich"/>
  <property name="build.dir" location="${basedir}/build"/>
  <property name="launcher.dir" location="${basedir}/launcher"/>
  <property name="7zip.exe.dir" location="${basedir}/7zip"/>
  <property name="sfx.dir" location="${basedir}/sfx"/>
  <property name="dist.dir" location="${basedir}/dist"/>

  <target name="clean.7zipexample" depends="clean.build.7zipexample,
      clean.dist.7zipexample" />

  <target name="clean.build.7zipexample">
    <delete includeemptydirs="true">
    <fileset dir="${build.dir}" includes="**/*" />
    </delete>
  </target>

  <target name="clean.dist.7zipexample">
    <delete includeemptydirs="true">
    <fileset dir="${dist.dir}" includes="**/*" />
      <fileset dir="${launcher.dir}" includes="app.jar" />
      <fileset dir="${launcher.dir}" includes="app.7z" />
    </delete>
  </target>

  <target name="package.7zipexample" depends="clean.7zipexample">

    <!-- compile the source -->
    <javac destdir="${build.dir}" srcdir="${src.dir}">
      <include name="**/*.java"/>
    </javac>

    <!-- create a runnable jar -->
    <jar destfile="${launcher.dir}/app.jar" manifest="Manifest.mf">
      <fileset dir="${build.dir}">
        <include name="**/*.class" />
      </fileset>
    </jar>

    <!-- compress all of the files we need to down with 7zip -->
    <exec executable="${7zip.exe.dir}/7z.exe" failonerror="true">
      <arg value="a" />
      <arg value="-t7z" />
      <arg value="-r" />
      <arg value="${launcher.dir}\app.7z" />
      <arg value="${launcher.dir}" />
    </exec>

    <!-- concat the files we need together to produce a binary
        launcher -->
    <concat destfile="${dist.dir}/start.exe" binary="yes">
      <fileset file="${sfx.dir}/7zSD.sfx" />
      <fileset file="${sfx.dir}/app.tag" />
      <fileset file="${launcher.dir}/app.7z" />
    </concat>

  </target>

</project>
```

You can always manually build the installer package yourself, but why bother if you have an Ant build file ready to do the work for you?  When you run the `package.7zipexample` build target in the example build file, the resulting ready to launch executable can be found at `dist\start.exe` &mdash; `start.exe` is your shippable application.  Again, no pre-installed Java Runtime Environment required!

### Changing the Icons and Version Information

If you use Oleg's `7zSD.sfx` launcher as is, you'll notice the icon attached to the resulting `.exe` is quite poor.  In all likelihood, you'll want to replace the icon with one for your application.  Doing so is quite easy with [Resource Hacker](http://www.angusj.com/resourcehacker/), a freeware utility to view, modify, rename, add, delete and extract resources in 32bit Windows executables and resource files.  Detailed instructions on how to replace the icon can be found [here on the 7zSD.sfx web-site](http://7zsfx.solta.ru/en/icon.html).  Note that you can also use Resource Hacker to edit the version and copyright details included in the resulting executable as shown below.

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/bundle-java-the-jre-and-launch-a-java-app-with-7zip-sfx/7zip-sfx-change-resources-thumb-400x284.png">

In summary, it's fairly straightforward to bundle and ship the Java Runtime Environment with your Java application using 7zip SFX.  Heck, Sun allows and even tells you how to redistribute the JRE with your applications (just read the LICENSE file provided with any JRE installation).

### On GitHub

All code shown here is available in my [7zip-sfx-java project on GitHub](https://github.com/markkolich/7zip-sfx-java).

<!--- tags: java, 7zip sfx -->