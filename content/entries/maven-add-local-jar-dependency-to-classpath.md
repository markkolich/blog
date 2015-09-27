I've been getting back into [Maven](https://maven.apache.org/) lately, converting the build system behind several of my [personal projects on GitHub](https://github.com/markkolich) into something a little more sane and well-travelled.  For reasons yet-to-be formally discussed, I've embarked on a mass migration away from [SBT](http://www.scala-sbt.org/) &mdash; albeit, I still have a number of published projects [backed](https://github.com/markkolich/spray-servlet-webapp) [by](https://github.com/markkolich/spring3-sbt) [SBT](https://github.com/markkolich/generate-indices).

### SBT Rant

Although I'm still using it sparingly, SBT has left a bitter taste in my mouth.  The long-and-short of it is that I'm tired of everything in SBT being a complicated puzzle backed by poor documentation &mdash; I just want to get stuff done.  I wish I had the *countless* hours of my life back that I spent figuring out how to accomplish very specific (yet seemingly common) tasks with SBT.  Build definitions *do not* need to be written in [Turing-complete languages](https://en.wikipedia.org/wiki/Turing_completeness), and in my humble opinion, SBT is a perfect example of [what not to do](https://en.wikipedia.org/wiki/Brainfuck).

*&lt;/rant&gt;*

### Maven

I was refactoring a personal project to use Maven the other day, and stumbled across a need to "add a local JAR to my classpath".  That is, I have a `.jar` file on disk [from many moons ago](https://code.google.com/p/gagawa/), that is not in any public Maven repository yet I need to add it to the `compile` scope of my project.

#### Bad: The `system` Scope
  
A quick search of the Interwebz clearly calls out a worst practice: using the Maven `system` scope.

The `system` scope was designed to deal with "system" related files &mdash; files sitting in some fixed location, like Java's core `rt.jar`.  To discourage bad behavior, the Maven contributors intentionally refused to make pathname expansion work correctly in the context of the `<systemPath>` tag in the `system` scope.  In other words, `${basedir}/lib/foo.jar` below will not resolve:

```xml
<dependencies>
    <!-- WRONG: DON'T DO THIS -->
    <dependency>
        <groupId>com.foo</groupId>
        <artifactId>bar</artifactId>
        <version>1.0</version>
        <scope>system</scope>
        <systemPath>${basedir}/lib/bar-1.0.jar</systemPath>
    </dependency>
</dependencies>
```

Don't do this.

#### Good: Use a Local Repository

The [best practice](http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html) is to "publish" the `.jar` file to a local Maven repository nested within the project.  Yes, you read that correctly, publish the `.jar` to a `~/.m2` like repo within your project that is checked into SCM!

Here's how...

On disk, your project probably looks something like this:

```
project/
  src/main/java
  src/main/resources
  src/test/java
  pom.xml
```

1) Create a `lib` directory in your project root &mdash; this `lib` directory will act as a local Maven repository within the project.

```
cd project
mkdir lib
```

2) Download the `.jar` file to disk, and use `mvn` to publish the `.jar` to the `lib` directory.

In this example, I'm publishing the [Gagawa](https://code.google.com/p/gagawa/) library I wrote and open-sourced many years ago.

```
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file \
  -Dfile=~/Desktop/gagawa-1.0.1.jar \
  -DgroupId=com.hp \
  -DartifactId=gagawa \
  -Dversion=1.0.1 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=lib
```

If all went well, you can find your artifact published inside of `lib`.

```
project$ find lib
lib
lib/com
lib/com/hp
lib/com/hp/gagawa
lib/com/hp/gagawa/1.0.1
lib/com/hp/gagawa/1.0.1/gagawa-1.0.1.jar
lib/com/hp/gagawa/1.0.1/gagawa-1.0.1.pom
lib/com/hp/gagawa/maven-metadata-local.xml
```

Note the structure here mimics what you'd find in `~/.m2`.

3) Now, in your `pom.xml`, declare the `lib` directory in your project a Maven repository.

```xml
<repositories>
    <repository>
        <id>my-local-repo</id>
        <url>file://${basedir}/lib</url>
    </repository>
</repositories>
```

4) And lastly, in your `pom.xml` declare a dependency on the local `.jar` like you would for any other classpath dependency.

```xml
<dependencies>
    <dependency>
        <groupId>com.hp.gagawa</groupId>
        <artifactId>gagawa</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

At runtime, Maven will consult the local repo at `${basedir}/lib` in addition to `~/.m2` and any other remote repositories you have defined.   

Ship it!
