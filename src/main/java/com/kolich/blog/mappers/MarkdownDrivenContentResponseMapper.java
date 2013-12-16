package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownDrivenContent;
import com.kolich.blog.mappers.handlers.FileNotFoundExceptionHandler;
import com.kolich.curacao.entities.mediatype.AbstractBinaryContentTypeCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class MarkdownDrivenContentResponseMapper
    extends RenderingResponseTypeMapper<MarkdownDrivenContent> {

    private static final Logger logger__ =
        getLogger(MarkdownDrivenContentResponseMapper.class);

    private final GitRepository git_;

    public MarkdownDrivenContentResponseMapper(final GitRepository git) {
        git_ = git;
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final MarkdownDrivenContent md) throws Exception {
        final String path = getPathToMarkdown(md).toString();
        final File entryFile = new File(git_.getRepo().getWorkTree(), path);
        try(final InputStream is = new FileInputStream(entryFile)) {
            final String html = new PegDownProcessor(Extensions.ALL)
                .markdownToHtml(IOUtils.toString(is, Charsets.UTF_8));
            renderMarkdown(response, html);
        } catch(Exception e) {
            logger__.warn("Failed to load/render content for: " + path, e);
            new FileNotFoundExceptionHandler().render(context, response);
        }
    }

    public abstract Path getPathToMarkdown(@Nonnull final MarkdownDrivenContent md);

    private static final void renderMarkdown(final HttpServletResponse response,
                                             final String html) throws Exception {
        RenderingResponseTypeMapper.renderEntity(response,
            new PegdownDrivenHtmlEntity(html));
    }

    private static final class PegdownDrivenHtmlEntity
        extends AbstractBinaryContentTypeCuracaoEntity {

        public PegdownDrivenHtmlEntity(final String html) {
            super(SC_OK, HTML_UTF_8, StringUtils.getBytesUtf8(html));
        }

    }

}
