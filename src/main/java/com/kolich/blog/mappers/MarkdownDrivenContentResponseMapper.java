package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Index;
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

    private static final String appContextPath__ =
        ApplicationConfig.getContextPath();

    private static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    private static final String TEMPLATE_ATTR_NAME = "name";
    private static final String TEMPLATE_ATTR_TITLE = "title";
    private static final String TEMPLATE_ATTR_HASH = "hash";
    private static final String TEMPLATE_ATTR_DATE = "date";
    private static final String TEMPLATE_ATTR_CONTENT = "content";
    private static final String TEMPLATE_ATTR_ENTRIES = "entries";

    private final Configuration config_;

    @Injectable
    public MarkdownDrivenContentResponseMapper(final GitRepository git)
        throws Exception {
        final File templateRoot = git.getFileRelativeToContentRoot("templates");
        config_ = new Configuration();
        config_.setDirectoryForTemplateLoading(templateRoot);
        config_.setDefaultEncoding(Charsets.UTF_8.name());
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final MarkdownContent md) throws Exception {
        try {
            final Template tmpl = config_.getTemplate(md.getTemplateName());
            try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
                final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
                tmpl.process(markdownContentToDataMap(md, tmpl), w);
                RenderingResponseTypeMapper.renderEntity(response,
                    new Utf8CompressedHtmlEntity(os));
            }
        } catch (Exception e) {
            logger__.warn("Content rendering exception: " + md, e);
            new ContentNotFoundExceptionHandler().render(context, response);
        }
    }

    private static final Map<String,Object> markdownContentToDataMap(
        final MarkdownContent md, final Template tmpl) throws Exception {
        final Map<String,Object> data = Maps.newLinkedHashMap();
        final Object name = tmpl.getCustomAttribute(TEMPLATE_ATTR_NAME);
        data.put(TEMPLATE_ATTR_NAME, (name == null) ? md.getName() : name);
        final Object title = tmpl.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        data.put(TEMPLATE_ATTR_TITLE, (title == null) ? md.getTitle() : title);
        final Object hash = tmpl.getCustomAttribute(TEMPLATE_ATTR_HASH);
        data.put(TEMPLATE_ATTR_HASH, (hash == null) ? md.getHash() : hash);
        final Object date = tmpl.getCustomAttribute(TEMPLATE_ATTR_DATE);
        data.put(TEMPLATE_ATTR_DATE, (date == null) ? md.getDateFormatted() : date);
        // Attach the Markdown content converted to a String to the data map.
        final MarkdownFile content;
        if((content = md.getContentFile()) != null) {
            data.put(TEMPLATE_ATTR_CONTENT, markdownToString(content));
        }
        // Only Index types get a list of entries attached to its data map.
        if(md instanceof Index) {
            data.put(TEMPLATE_ATTR_ENTRIES, ((Index)md).getEntries());
        }
        data.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        return data;
    }

    public static final String markdownToString(final MarkdownFile mdf)
        throws Exception {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(readFileToString(mdf.getFile(), Charsets.UTF_8));
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

        private static final String compressHtml(final String uncompressed) {
            final HtmlCompressor compressor = new HtmlCompressor();
            compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
            return compressor.compress(uncompressed);
        }

    }

}
