
package com.kolich.blog.controllers

import com.kolich.curacao.annotations.{Injectable, Controller}
import com.kolich.blog.git._
import com.kolich.curacao.annotations.methods.GET
import com.kolich.curacao.annotations.parameters.Path
import scala.collection.JavaConverters._
import scala.io.Codec.UTF8
import scala.reflect.io.File
import com.gitblit.utils.JGitUtils
import org.eclipse.jgit.diff.DiffEntry.ChangeType
import com.kolich.blog.ApplicationConfig
import java.util.{TimeZone, Date}
import com.kolich.common.date.RFC822DateFormat
import org.pegdown.{Extensions, PegDownProcessor}
import com.kolich.curacao.entities.mediatype.ArbitraryBinaryTypeCuracaoEntity
import com.google.common.net.MediaType

@Controller
final class Index @Injectable()(blogRepo: BlogRepository) {

  private[this] final val markdownDir = ApplicationConfig.markdownDir

  @GET("/")
  def index = {
    blogRepo.repo.toString
  }

//  @GET("/{content}/**")
//  def content(@Path("content") content: String) = {
//    content
//  }

  @GET("/git")
  def about = {
    val commits = blogRepo.git.log.call.asScala
    commits.map(commit => {
      val files = JGitUtils.getFilesInCommit(blogRepo.repo, commit).asScala
      files.filter(file => {
        val name = file.name
        val ct = file.changeType
        name.startsWith(markdownDir) && ct.equals(ChangeType.ADD)
      }).map(_.name).map(file => {
        val format = RFC822DateFormat.getNewInstance
        format.setTimeZone(TimeZone.getTimeZone("GMT-8"))
        commit.getId.name + " " + format.format(new Date(commit.getCommitTime.toLong * 1000L)) + " " + file
      }).mkString("\n")
    }).mkString("\n\n")
  }

  @GET("/entry/{entry}")
  def entry(@Path("entry") entry: String) = {
    val markdown = blogRepo.repoDir / markdownDir / entry addExtension "md"
    val html = new PegDownProcessor(Extensions.ALL).markdownToHtml(File(markdown).slurp(UTF8))
    new ArbitraryBinaryTypeCuracaoEntity(MediaType.HTML_UTF_8, html.getBytes("UTF-8"))
  }

}
