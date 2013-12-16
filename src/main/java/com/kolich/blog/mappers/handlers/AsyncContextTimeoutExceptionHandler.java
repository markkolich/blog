package com.kolich.blog.mappers.handlers;

import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.mediatype.document.TextPlainCuracaoEntity;
import com.kolich.curacao.exceptions.async.AsyncContextTimeoutException;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@ControllerReturnTypeMapper(AsyncContextTimeoutException.class)
public final class AsyncContextTimeoutExceptionHandler
    extends RenderingResponseTypeMapper<AsyncContextTimeoutException> {

    @Override
    public void render(final AsyncContext context,
                       final HttpServletResponse response,
                       @Nonnull final AsyncContextTimeoutException entity) throws Exception {
        RenderingResponseTypeMapper.renderEntity(response,
            new TextPlainCuracaoEntity(SC_INTERNAL_SERVER_ERROR,
                "Oops! The server was not able to produce a " +
                "timely response to your request."));
    }

}
