
package com.kolich.blog.controllers

import com.kolich.curacao.annotations.{Injectable, Controller}
import com.kolich.blog.git._
import com.kolich.curacao.annotations.methods.GET
import com.kolich.curacao.annotations.parameters.Path
import scala.collection.JavaConverters._
import com.gitblit.utils.JGitUtils
import org.eclipse.jgit.diff.DiffEntry.ChangeType

@Controller
final class Index @Injectable()(blogRepo: BlogRepository) {

  @GET("/")
  def index = {
    blogRepo.repo.toString
  }

  @GET("/{content}/**")
  def content(@Path("content") content: String) = {
    content
  }

  @GET("/git")
  def about = {
    val commits = blogRepo.git.log.addPath("src").call.asScala
    commits.map(commit => {
      val files = JGitUtils.getFilesInCommit(blogRepo.repo, commit).asScala
      files.filter(file => {
        val name = file.name
        val ct = file.changeType
        name.startsWith("src") && (ct.equals(ChangeType.ADD) || ct.equals(ChangeType.MODIFY))
      }).map(_.name).map(file => {
        commit.getId.name + " " + file
      }).mkString("\n")
    }).mkString("\n\n")
  }

}
