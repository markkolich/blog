package com.kolich.blog.mappers.handlers;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.kolich.blog.components.FreeMarkerCache;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.mappers.MarkdownDrivenContentResponseMapper.Utf8CompressedHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import freemarker.template.Template;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

@ControllerReturnTypeMapper(ContentNotFoundException.class)
public final class ContentNotFoundExceptionHandler
    extends RenderingResponseTypeMapper<ContentNotFoundException> {

    private final FreeMarkerCache freemarker_;

    @Injectable
    public ContentNotFoundExceptionHandler(final FreeMarkerCache freemarker) {
        freemarker_ = freemarker;
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final ContentNotFoundException entity) throws Exception {
        final Template tp = freemarker_.getConfig()
            .getTemplate("errors/404-not-found.ftl");
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(ImmutableMap.of(), w);
            renderEntity(response, new Utf8CompressedHtmlEntity(os));
        }
    }

}
