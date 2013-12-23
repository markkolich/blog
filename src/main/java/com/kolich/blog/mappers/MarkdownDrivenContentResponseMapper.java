package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.MarkdownFile;
import com.kolich.blog.mappers.handlers.ContentNotFoundExceptionHandler;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.codec.binary.StringUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(MarkdownContent.class)
public final class MarkdownDrivenContentResponseMapper
    extends RenderingResponseTypeMapper<MarkdownContent> {

    private static final Logger logger__ =
        getLogger(MarkdownDrivenContentResponseMapper.class);

    private final Configuration config_;

    @Injectable
    public MarkdownDrivenContentResponseMapper(final GitRepository git) throws Exception {
        final File templateRoot = git.getFileRelativeToMarkdownRoot("templates");
        config_ = new Configuration();
        config_.setDirectoryForTemplateLoading(templateRoot);
        config_.setDefaultEncoding(Charsets.UTF_8.name());
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final MarkdownContent md) throws Exception {
        try {
            final Template t = config_.getTemplate(md.getTemplateName());
            try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
                final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
                t.process(markdownContentToDataMap(md), w);
                RenderingResponseTypeMapper.renderEntity(response,
                    new Utf8CompressedHtmlEntity(os));
            }
        } catch (Exception e) {
            logger__.debug("Content rendering exception: " + md, e);
            new ContentNotFoundExceptionHandler().render(context, response);
        }
    }

    private static final Map<String,Object> markdownContentToDataMap(
        final MarkdownContent md) throws Exception {
        final Map<String,Object> data = Maps.newLinkedHashMap();
        data.put("name", md.getName());
        data.put("title", md.getTitle());
        data.put("hash", md.getHash());
        data.put("date", md.getDateFormatted());
        data.put("content", markdownToString(md.getContent()));
        return data;
    }

    public static final String markdownToString(final MarkdownFile mdf)
        throws Exception {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(readFileToString(mdf.getFile(), Charsets.UTF_8));
    }

    private static final String compressHtml(final String uncompressed) {
        final HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
        return compressor.compress(uncompressed);
    }

    private static final class Utf8CompressedHtmlEntity
        extends AppendableCuracaoEntity {

        private static final String HTML_UTF_8_STRING = HTML_UTF_8.toString();

        private final String html_;

        public Utf8CompressedHtmlEntity(final String html) {
            super();
            html_ = compressHtml(html);
        }

        public Utf8CompressedHtmlEntity(final byte[] html) {
            this(StringUtils.newStringUtf8(html));
        }

        public Utf8CompressedHtmlEntity(final ByteArrayOutputStream html) {
            this(html.toByteArray());
        }

        @Override
        public final int getStatus() {
            return SC_OK;
        }

        @Override
        public final String getContentType() {
            return HTML_UTF_8_STRING;
        }

        @Override
        public final void toWriter(final Writer writer) throws Exception {
            try(final Reader reader = new StringReader(html_)) {
                copyLarge(reader, writer);
            }
        }

    }

}
