I hate seeing compiler warnings in code, and anyone who argues that ignoring them is a fine software engineering policy, should be swiftly relieved of their position.  Warnings call out mistakes, and occasional blatant stupidity that should not be ignored &mdash; heck just Google ["pay attention to compiler warnings"](https://www.google.com/search?q=pay+attention+to+compiler+warnings) for some fun anecdotes.  Folks in software are generally helpful, and compiler writers don't inject annoying warnings because they are mean-spirited.  Instead, people want to help, and consequently, compiler warnings are there to help.  I've personally worked on several stacks with literally thousands of compiler warnings peppered throughout the code &mdash; it's miraculous that some of those applications worked at all.

To combat warning hell, I've made it a personal best practice to do two things:

1. In mature code bases, never introduce more warnings and never ignore existing warnings I happen to stumble across. Ever. If I see a warning in an area of code that I'm working on, I'll clean it up &mdash; no excuses.
2. In new projects, starting from scratch, set my build tool to immediately fail the build on any warning. In other words, *treat warnings as compilation errors*!

The latter is surprisingly easy and very effective at forcibly setting a high bar before entropy can gain a foothold.

Here's how, with a few popular build tools:

### Ant

If you're still using Ant, set a series of `<compilerarg>` tags in your `<javac>` tasks. Of course, this goes in your `build.xml`:

```xml
<javac srcdir="${src.dir}" destdir="${classes.dir}" classpathref="libraries">
    <compilerarg value="-Xlint:all"/>
    <compilerarg value="-Xlint:-processing"/>
    <compilerarg value="-Xlint:-serial"/>
    <compilerarg value="-Werror"/>
</javac> 
```

### Maven

Using the `maven-compiler-plugin` add a few `<compilerArgs>` to your configuration within your `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.3</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <compilerArgs>
            <arg>-Xlint:all</arg>
            <arg>-Xlint:-processing</arg>
            <arg>-Xlint:-serial</arg>
            <arg>-Werror</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Gradle

To fail the build on any compiler warning, in main source and in test source, set this in your `build.gradle`:

```groovy
tasks.withType(JavaCompile) {
    options.compilerArgs << "-Xlint:all" << "-Xlint:-processing" << "-Xlint:-serial" << "-Werror"
}
```

### SBT

In the unlikely event that you're building a pure Java project or Java source with SBT, set this in your `project/Build.scala`:

```scala
lazy val projectSettings = Defaults.coreDefaultSettings ++ Seq(
  scalacOptions ++= Seq(
    "-deprecation", "-unchecked", "-feature", "-Xlint", "-Xfatal-warnings", "-encoding", "utf8"
  ),
  javacOptions ++= Seq("-Xlint:all,-processing,-serial", "-Werror", "-encoding", "utf8", "-g")
)
```

The Scala compiler `scalac` equivalent to `-Werror` is `-Xfatal-warnings` ([apparently](https://tpolecat.github.io/2014/04/11/scalac-flags.html)).

### A few notes

The magic is in `-Werror` which is documented [here](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html). When set, `-Werror` terminates compilation when warnings are found.

I'm also passing `-Xlint:-processing` which disables any annotation processor warnings from JARs on the compile classpath.  And lastly, `-Xlint:-serial` disables any warnings complaining of `Serializable` classes that do not have an explicit `serialVersionUID` field.  Ok, yes, certainly one could argue that ignoring complaints about a missing `serialVersionUID` field is dangerous, but I'll let you be the judge.

Cheers!

<!--- tags: java, scala, ant, maven, gradle, sbt -->