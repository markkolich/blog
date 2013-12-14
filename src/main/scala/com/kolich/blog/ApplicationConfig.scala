package com.kolich.blog

import com.typesafe.scalalogging.slf4j.Logging
import com.typesafe.config.{ ConfigFactory, Config }
import scala.reflect.io.Path

object ApplicationConfig extends Logging {

  private[this] val jettyHome: Option[String] = Option(System.getProperty("jetty.home"))
  private[this] val catalinaHome: Option[String] = Option(System.getProperty("catalina.home"))

  private[this] lazy val c: Config = {

    def loadOverrideConfig: Option[Config] = {
      val file: Option[Path] = jettyHome match {
        case Some(jetty) => Some((Path(jetty) / "conf" / "blog.conf"))
        case _ => catalinaHome match {
          case Some(catalina) => Some((Path(catalina) / "conf" / "blog.conf"))
          case _ => None
        }
      }
      file match {
        case Some(f) => try {
          if(f.exists) {
            Some(ConfigFactory.parseFile(f.jfile))
          } else {
            None
          }
        } catch {
          case e: Exception => {
            logger.error("Failed to load and parse override configuration " +
              "file: " + f.toCanonical, e)
            None
          }
        }
        case _ => None
      }
    }

    // Load the "default configuration" from application.conf
    val applicationConf = ConfigFactory.load

    // Load the "override configuration", if one exists
    val overrideConfig = loadOverrideConfig

    overrideConfig match {
      case Some(overridez) => overridez.withFallback(applicationConf)
      case _ => applicationConf
    }

  }

  // Blog configuration

  private[this] lazy val blogConfig: Config = c getConfig("blog")

  final val isDevMode: Boolean = blogConfig getBoolean("dev-mode")

  final val blogRepoUrl: String = blogConfig getString("clone.url")
  final val cloneFromScratchOnStartup: Boolean = blogConfig getBoolean("clone.from-scratch-on-startup")
  final val clonePath: String = blogConfig getString("clone.path")

  final val markdownDir: String = blogConfig getString("markdown-dir")

}
