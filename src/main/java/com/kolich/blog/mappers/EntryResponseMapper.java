package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Entry;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.mediatype.AbstractBinaryContentTypeCuracaoEntity;
import com.kolich.curacao.entities.mediatype.ArbitraryBinaryTypeCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.FileSystems;

@ControllerReturnTypeMapper(Entry.class)
public final class EntryResponseMapper
    extends RenderingResponseTypeMapper<Entry> {

    private static final String markdownDir__ =
        ApplicationConfig.getMarkdownDir();

    private final GitRepository git_;

    @Injectable
    public EntryResponseMapper(final GitRepository git) {
        git_ = git;
    }

    @Override
    public void render(final AsyncContext context,
                       final HttpServletResponse response,
                       @Nonnull final Entry entry) throws Exception {
        final String path = FileSystems.getDefault()
            .getPath(markdownDir__, entry.getName() + ".md")
            .toString();
        final File entryFile = new File(git_.getRepo().getWorkTree(), path);
        try(final InputStream is = new FileInputStream(entryFile)) {
            final String html = new PegDownProcessor(Extensions.ALL)
                .markdownToHtml(IOUtils.toString(is, Charsets.UTF_8));
            RenderingResponseTypeMapper.renderEntity(response,
                new PegdownDrivenHtmlEntity(html));
        } catch(Exception e) {
            new FileNotFoundExceptionHandler().render(context, response);
        }
    }

    private static final class PegdownDrivenHtmlEntity
        extends AbstractBinaryContentTypeCuracaoEntity {

        public PegdownDrivenHtmlEntity(final String html) {
            super(HttpServletResponse.SC_OK,
                MediaType.HTML_UTF_8,
                StringUtils.getBytesUtf8(html));
        }

    }

}
