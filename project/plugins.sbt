addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.5.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")

addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.2.2")

// Add dependencies for JS and CSS "compilation".
// Note that this adds the dependencies only for the build process (makes
// these libraries available to the build classpath, namely in Build.scala).
// This does ~not~ add the dependencies to the compile time or run time classpaths.

libraryDependencies += "org.apache.ant" % "ant" % "1.9.2"

libraryDependencies += "com.google.javascript" % "closure-compiler" % "v20131014"

libraryDependencies += "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7"
