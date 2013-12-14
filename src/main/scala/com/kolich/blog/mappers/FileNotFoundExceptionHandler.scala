package com.kolich.blog.mappers

import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper
import java.io.FileNotFoundException
import javax.servlet.AsyncContext
import javax.servlet.http.HttpServletResponse
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper
import com.kolich.curacao.entities.mediatype.document.TextPlainCuracaoEntity

@ControllerReturnTypeMapper(classOf[FileNotFoundException])
final class FileNotFoundExceptionHandler
  extends RenderingResponseTypeMapper[FileNotFoundException] {

  private[this] lazy val notFound = new TextPlainCuracaoEntity(404, "Not Found")

  def render(context: AsyncContext,
             response: HttpServletResponse,
             entity: FileNotFoundException) = {
    RenderingResponseTypeMapper.renderEntity(response, notFound)
  }

}
