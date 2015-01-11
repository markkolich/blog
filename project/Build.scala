/**
 * Copyright (c) 2014 Mark S. Kolich
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

import sbt._
import sbt.Keys._

import de.johoop.findbugs4sbt.FindBugs._
import de.johoop.findbugs4sbt.ReportType

import com.earldouglas.xsbtwebplugin._
import PluginKeys._
import WebPlugin._

object Dependencies {

  // Internal dependencies
  
  private val kolichCommon = "com.kolich" % "kolich-common" % "0.2" % "compile"

  // External dependencies

  private val curacao = "com.kolich.curacao" % "curacao" % "2.8.6" % "compile"
  private val curacaoGson = "com.kolich.curacao" % "curacao-gson" % "2.8.6" % "compile"

  // Jetty 9 stable, version 9.2.6.v20141205 (as of 12/20/14)
  private val jettyVersion = "9.2.6.v20141205"
  private val jettyWebApp = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container"
  private val jettyPlus = "org.eclipse.jetty" % "jetty-plus" % jettyVersion % "container"
  private val jettyJsp = "org.eclipse.jetty" % "jetty-jsp" % jettyVersion % "container"
  
  private val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided" // Provided by container

  private val logback = "ch.qos.logback" % "logback-core" % "1.1.2" % "compile"
  private val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.2" % "compile" // An Slf4j impl
  private val slf4j = "org.slf4j" % "slf4j-api" % "1.7.7" % "compile"

  private val jGit = "org.eclipse.jgit" % "org.eclipse.jgit" % "3.5.0.201409260305-r" % "compile"
  private val gitblit = "com.gitblit" % "gitblit" % "1.6.0" % "compile" intransitive()

  private val pegdown = "org.pegdown" % "pegdown" % "1.4.2" % "compile"
  private val freemarker = "org.freemarker" % "freemarker" % "2.3.21" % "compile"
  private val htmlCompressor = "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2" % "compile" intransitive()

  private val asyncHttpClient = "com.ning" % "async-http-client" % "1.8.14" % "compile"

  val deps = Seq(
    kolichCommon,
    curacao, curacaoGson,
    jettyWebApp, jettyPlus, jettyJsp, servlet,
    logback, logbackClassic, slf4j,
    jGit, gitblit,
    pegdown,
    freemarker, htmlCompressor,
    asyncHttpClient
  )

}

object Resolvers {

  private val kolichRepo = "Kolich repo" at "http://markkolich.github.io/repo"
  private val gitblitRepo = "Gitblit repo" at "http://gitblit.github.io/gitblit-maven"

  val depResolvers = Seq(kolichRepo, gitblitRepo)

}

sealed trait AntHelpers {

  import org.apache.tools.ant.types._
  import org.apache.tools.ant.taskdefs._

  def getFileList(dir: File, files: Seq[String]): FileList = {
    val list = new FileList()
    list.setDir(dir)
    list.setFiles(files.mkString(", "))
    list
  }
  def getFileList(dir: File, file: String): FileList = {
    getFileList(dir, Seq(file))
  }
  def concatenate(dest: File, fileList: FileList) {
    val concat = new Concat()
    concat.setDestfile(dest)
    concat.addFilelist(fileList)
    concat.execute
  }

}

object PackageJs extends AntHelpers {

  import org.apache.tools.ant.types._
  import com.google.javascript.jscomp.ant._

  private lazy val packageJs = TaskKey[Unit](
    "package:js",
    "Compile and package application JavaScript with Google's Closure Compiler."
  )

  val settings = Seq(
    packageJs <<= baseDirectory(new File(_, "content/static")) map { base =>
      val js = base / "js"
      val build = js / "build"
      val release = base / "release"
      val libs = getFileList(js / "lib", Seq(
        "json2.js",
        "jquery-1.10.2.min.js",
        "jquery.timeago-1.4.1.js",
        //"jquery.localtime-0.8.0.js",
        // I discovered I wasn't actually using any of Bootstrap's JS
        // in the web-application, so there's no need to include it in
        // the apps "released" JavaScript bundle.
        //"bootstrap.min.js",
        "prettify.js"))
      val sources = getFileList(js, Seq(
        "kolich.js",
        "kolich.blog.js",
        "kolich.blog.twitter.js",
        "kolich.blog.translate.js",
        "kolich.blog.provider.js"))
      println("Compiling JavaScript...")
      // Concat libs together
      concatenate(build / "blog.lib.js", libs)
      // Concat sources together
      concatenate(build / "blog.js", sources)
      // Actual closer compiler compilation
      closureCompile(release / "blog.js", getFileList(build, Seq(
        "blog.lib.js",
        "blog.js")))
      IO.delete(build) // recursive
    },
    compile in Compile <<= (compile in Compile) dependsOn (packageJs),
    packageWar in Compile <<= (packageWar in Compile) dependsOn (packageJs)
  )

  private def closureCompile(output: File,
                             sources: FileList,
                             externs: Option[FileList] = None,
                             compilationLevel: String = "simple") {
    val compile = new CompileTask()
    compile.setCompilationLevel(compilationLevel) // Could be "simple" or "advanced"
    compile.setWarning("quiet") // Could be "verbose"
    compile.setDebug(false)
    compile.setOutput(output)
    compile.addSources(sources)
    compile.setForceRecompile(false) // False is the default, but here for doc purposes
    if (externs != None) {
      compile.addExterns(externs.get)
    }
    compile.execute
  }

}

object PackageCss extends AntHelpers {

  import com.yahoo.platform.yui.compressor._

  private lazy val packageCss = TaskKey[Unit](
    "package:css",
    "Minify CSS using YUI's CSS compressor."
  )

  val settings = Seq(
    packageCss <<= baseDirectory(new File(_, "content/static")) map { base =>
      val css = base / "css"
      val build = css / "build"
      val release = base / "release"
      // Create the "release" directory if it does not exist (YUI does not
      // create this destination directory for us... it has to exist before
      // we attempt to use it).
      IO.createDirectory(release)
      println("Compiling CSS...")
      val libs = getFileList(css / "lib", Seq(
        //"bootstrap.min.css",
        //"bootstrap-theme.min.css",
        "font-awesome.min.css",
        "spacelab.css",
        "prettify.css",
        "prettify-desert.css"))
      val sources = getFileList(css, Seq(
        "blog.css"))
      // Concat libs together
      concatenate(build / "blog.lib.css", libs)
      // Concat sources together
      concatenate(build / "blog.src.css", sources)
      // Final file for "compilation"
      concatenate(build / "blog.css", getFileList(build,
        Seq("blog.lib.css", "blog.src.css")))
      // Actually do the compression
      compress(build / "blog.css", release / "blog.css")
      IO.delete(build) // recursive
    },
    compile in Compile <<= (compile in Compile) dependsOn (packageCss),
    packageWar in Compile <<= (packageWar in Compile) dependsOn (packageCss)
  )

  private def compress(input: File, output: File) {
    import java.io._
    // Setup the input reader and the output writer.
    var reader:Reader = null
    var writer:Writer = null
    try {
      reader = new InputStreamReader(new FileInputStream(input), "UTF-8")
      writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8")
      new CssCompressor(reader).compress(writer, -1)
    } finally {
      if(reader != null) { reader.close }
      if(writer != null) { writer.close }
    }
  }

}

object Blog extends Build {

  import Dependencies._
  import Resolvers._

  private val aName = "blog"
  private val aVer = "1.2-SNAPSHOT"
  private val aOrg = "com.kolich"

  lazy val blog: Project = Project(
    aName,
    new File("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
      version := aVer,
      organization := aOrg,
      scalaVersion := "2.10.3",
      javacOptions ++= Seq(
        "-Xlint:all,-path", "-g"/*,
        // Java "cross compiling" against Java 6. Note you need to provide the "rt"
        // and "jce" (Java crypto extension) JAR's and place them in a place where
        // 'javac' can pick them up as specified by the "-bootclasspath" compiler
        // argument.
        "-bootclasspath", "jdk/jdk1.6.0_45_rt.jar:jdk/jdk1.6.0_45_jce.jar",
        "-source", "1.6", "-target", "1.6"*/
      ),
      resolvers := depResolvers,
      shellPrompt := { (state: State) => { "%s:%s> ".format(aName, aVer) } },
      // True to export the packaged JAR instead of just the compiled .class files.
      exportJars := true,
      // Disable using the Scala version in output paths and artifacts.
      // When running 'publish' or 'publish-local' SBT would append a
      // _<scala-version> postfix on artifacts. This turns that postfix off.
      crossPaths := false,
      // Keep the scala-lang library out of the generated POM's for this artifact. 
      autoScalaLibrary := false,
      // Only add src/main/java and src/test/java as source folders in the project.
      // Not a "Scala" project at this time.
      unmanagedSourceDirectories in Compile <<= baseDirectory(new File(_, "src/main/java"))(Seq(_)),
      unmanagedSourceDirectories in Test <<= baseDirectory(new File(_, "src/test/java"))(Seq(_)),
      // Override the SBT default "target" directory for compiled classes.
      classDirectory in Compile <<= baseDirectory(new File(_, "target/classes")),
      // Add the local 'config' directory to the classpath at runtime,
      // so anything there will ~not~ be packaged with the application deliverables.
      // Things like application configuration .properties files go here in
      // development and so these will not be packaged+shipped with a build.
      // But, they are still available on the classpath during development,
      // like when you run Jetty via the xsbt-web-plugin that looks for some
      // configuration file or .properties file on the classpath.
      unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "config") },
      // Do not bother trying to publish artifact docs (javadoc). Meh.
      publishArtifact in packageDoc := false,
      // Override the global name of the artifact.
      artifactName <<= (name in (Compile, packageBin)) { projectName =>
        (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          var newName = projectName
          if (module.revision.nonEmpty) {
            newName += "-" + module.revision
          }
          newName + "." + artifact.extension
      },
      // Override the default 'package' path used by SBT. Places the resulting
      // JAR into a more meaningful location.
      artifactPath in (Compile, packageBin) ~= { defaultPath =>
        file("dist") / defaultPath.getName
      },
      // Override the default 'test:package' path used by SBT. Places the
      // resulting JAR into a more meaningful location.
      artifactPath in (Test, packageBin) ~= { defaultPath =>
        file("dist") / "test" / defaultPath.getName
      },
      libraryDependencies ++= deps,
      retrieveManaged := true) ++
      // Findbugs settings
      findbugsSettings ++ Seq(
        findbugsReportType := Some(ReportType.Html),
        findbugsReportPath := Some(baseDirectory(new File(_, "dist/findbugs.html")).value)
      ) ++
      // xsbt-web-plugin settings
      webSettings ++ Seq(
        // Overrides the default context path used for this project.  By
        // default, the context path is "/", but here we're overriding it
        // so that the application is available under "/blog" instead.
        apps in container.Configuration <<= (deployment in DefaultConf) map {
          d => Seq("/blog" -> d)
        },
        warPostProcess in Compile <<= fullClasspath in Compile map { fc => {
          target =>
            val webinf = target / "WEB-INF"
            IO.delete(webinf / "work") // recursive
            IO.delete(webinf / "classes") // recursive
          }
        },
        // Change the location of the packaged WAR file as generated by the
        // xsbt-web-plugin.
        artifactPath in (Compile, packageWar) ~= { defaultPath =>
          file("dist") / defaultPath.getName
        }
      ) ++
      PackageJs.settings ++ PackageCss.settings
  )

}
