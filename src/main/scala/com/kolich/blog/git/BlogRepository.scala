package com.kolich.blog.git

import com.typesafe.scalalogging.slf4j.Logging
import com.kolich.curacao.handlers.components.CuracaoComponent
import javax.servlet.ServletContext
import com.kolich.blog.ApplicationConfig
import scala.reflect.io.{File, Path}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import com.kolich.curacao.annotations.Component

@Component
final class BlogRepository extends CuracaoComponent with Logging {

  private[this] val devMode: Boolean = ApplicationConfig.isDevMode

  private[this] val clonePath: String = ApplicationConfig.clonePath
  private[this] val repoUrl: String = ApplicationConfig.blogRepoUrl
  private[this] val cloneFromScratchOnStartup: Boolean = ApplicationConfig.cloneFromScratchOnStartup

  var repoDir: Path = _
  var repo: Repository = _
  var git: Git = _

  override def initialize(context: ServletContext) = {
    // Discover the right path to the local repository on disk.
    repoDir = Path(devMode match {
      case true => System.getProperty("user.dir")
      case _ => clonePath.startsWith(File.separator) match {
        case true => clonePath
        case _ => context.getRealPath(File.separator + clonePath)
      }
    })
    logger.info("Using repository clone path: " + repoDir.toCanonical)
    val clone: Boolean = (repoDir.notExists || cloneFromScratchOnStartup)
    if(!devMode && clone) {
      logger.info("Clone path does not exist, or we were asked to re-clone" +
        "fresh on startup. So, cloning from: " + repoUrl)
      repoDir.deleteRecursively()
      Git.cloneRepository().setURI(repoUrl).setDirectory(repoDir.jfile)
        .setBranch("master").call
    }
    repo = new FileRepositoryBuilder().setWorkTree(repoDir.jfile)
      .readEnvironment.build
    git = new Git(repo)
    if(!devMode) {
      git.pull.call
    }
    logger.info("Successfully initialized Git repository: " + repo)
  }

  override def destroy(context: ServletContext) = {
    repo.close
  }

}
