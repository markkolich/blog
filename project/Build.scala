/**
 * Copyright (c) 2013 Mark S. Kolich
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

import com.earldouglas.xsbtwebplugin._
import PluginKeys._
import WebPlugin._

object Dependencies {

  // Internal dependencies
  
  private val kolichCommon = "com.kolich" % "kolich-common" % "0.1.0" % "compile"

  // External dependencies

  private val curacao = "com.kolich.curacao" % "curacao" % "2.0-M7" % "compile"
  private val curacaoGson = "com.kolich.curacao" % "curacao-gson" % "2.0-M7" % "compile"

  // Jetty 9.1 "stable", version 9.1.0.v20131115 (as of 11/25/13)
  private val jettyWebApp = "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container"
  private val jettyPlus = "org.eclipse.jetty" % "jetty-plus" % "9.1.0.v20131115" % "container"
  private val jettyJsp = "org.eclipse.jetty" % "jetty-jsp" % "9.1.0.v20131115" % "container"
  
  private val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided" // Provided by container

  private val typesafeConfig = "com.typesafe" % "config" % "1.0.2" % "compile"

  private val logback = "ch.qos.logback" % "logback-core" % "1.0.13" % "compile"
  private val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.13" % "compile" // An Slf4j impl
  private val slf4j = "org.slf4j" % "slf4j-api" % "1.7.5" % "compile"

  //private val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1" % "compile"
  //private val commonsCodec = "commons-codec" % "commons-codec" % "1.6" % "compile"

  private val jGit = "org.eclipse.jgit" % "org.eclipse.jgit" % "3.1.0.201310021548-r" % "compile"
  private val gitblit = "com.gitblit" % "gitblit" % "1.3.2" % "compile" intransitive()
  private val pegdown = "org.pegdown" % "pegdown" % "1.4.2" % "compile"

  val deps = Seq(kolichCommon,
    curacao, curacaoGson,
    jettyWebApp, jettyPlus, jettyJsp, servlet,
    typesafeConfig,
    //commonsLang, commonsCodec,
    logback, logbackClassic, slf4j,
    jGit, gitblit,
    pegdown)

}

object Resolvers {

  private val kolichRepo = "Kolich repo" at "http://markkolich.github.io/repo"
  private val gitblitRepo = "Gitblit repo" at "http://gitblit.github.io/gitblit-maven"

  val depResolvers = Seq(kolichRepo, gitblitRepo)

}

object Blog extends Build {

  import Dependencies._
  import Resolvers._

  private val aName = "blog"
  private val aVer = "1.0-SNAPSHOT"
  private val aOrg = "com.kolich"

  lazy val blog: Project = Project(
    aName,
    new File("."),
    settings = Defaults.defaultSettings ++ Seq(resolvers := depResolvers) ++ Seq(
      version := aVer,
      organization := aOrg,
      scalaVersion := "2.10.3",
      javacOptions ++= Seq(
        "-Xlint", "-g"/*,
        // Java "cross compiling" against Java 6. Note you need to provide the "rt"
        // and "jce" (Java crypto extension) JAR's and place them in a place where
        // 'javac' can pick them up as specified by the "-bootclasspath" compiler
        // argument.
        "-bootclasspath", "jdk/jdk1.6.0_45_rt.jar:jdk/jdk1.6.0_45_jce.jar",
        "-source", "1.6", "-target", "1.6"*/
      ),
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
      // xsbt-web-plugin settings
      webSettings ++
      Seq(warPostProcess in Compile <<= (target) map {
        (target) => { () => {
          val webinf = target / "webapp" / "WEB-INF"
          IO.delete(webinf / "work") // recursive
          IO.delete(webinf / "classes") // recursive
        }}
      },
      // Change the location of the packaged WAR file as generated by the
      // xsbt-web-plugin.
      artifactPath in (Compile, packageWar) ~= { defaultPath =>
        file("dist") / defaultPath.getName
      })
  )

}
