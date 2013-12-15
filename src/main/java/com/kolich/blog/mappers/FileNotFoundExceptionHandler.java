package com.kolich.blog.mappers;

import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.entities.mediatype.document.TextPlainCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;

@ControllerReturnTypeMapper(FileNotFoundException.class)
public final class FileNotFoundExceptionHandler
    extends RenderingResponseTypeMapper<FileNotFoundException> {

    private static final CuracaoEntity notFound__ =
        new TextPlainCuracaoEntity(404, "Not Found");

    @Override
    public void render(final AsyncContext context,
                       final HttpServletResponse response,
                       @Nonnull final FileNotFoundException entity) throws Exception {
        RenderingResponseTypeMapper.renderEntity(response, notFound__);
    }

    public void render(final AsyncContext context,
                       final HttpServletResponse response) throws Exception {
        render(context, response, null);
    }

}
