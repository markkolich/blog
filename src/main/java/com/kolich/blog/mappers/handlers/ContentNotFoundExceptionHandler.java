package com.kolich.blog.mappers.handlers;

import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.entities.mediatype.document.TextPlainCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;

@ControllerReturnTypeMapper(ContentNotFoundException.class)
public final class ContentNotFoundExceptionHandler
    extends RenderingResponseTypeMapper<ContentNotFoundException> {

    private static final CuracaoEntity notFound__ =
        new TextPlainCuracaoEntity(404, "Not Found");

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final ContentNotFoundException entity) throws Exception {
        RenderingResponseTypeMapper.renderEntity(response, notFound__);
    }

    public final void render(final AsyncContext context,
                             final HttpServletResponse response) throws Exception {
        render(context, response, null);
    }

}
