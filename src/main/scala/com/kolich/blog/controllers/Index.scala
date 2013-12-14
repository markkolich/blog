package com.kolich.blog.controllers

import com.kolich.curacao.annotations.{Injectable, Controller}
import com.kolich.blog.git._
import com.kolich.curacao.annotations.methods.GET
import com.kolich.curacao.annotations.parameters.Path
import scala.collection.JavaConverters._
import com.gitblit.utils.JGitUtils

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
      files.map(_.name).filter(_.startsWith("src")).map(file => {
        commit.getId.name + " " + file
      }).mkString("\n")
    }).mkString("\n\n")
  }

}
