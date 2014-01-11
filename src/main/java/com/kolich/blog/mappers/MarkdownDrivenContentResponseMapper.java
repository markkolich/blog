package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerCache;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.MarkdownFile;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
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
    private static final String TEMPLATE_ATTR_COMMIT = "commit";
    private static final String TEMPLATE_ATTR_DATE = "date";
    private static final String TEMPLATE_ATTR_CONTENT = "content";

    private static final String TEMPLATE_ATTR_ENTRIES = "entries";
    private static final String TEMPLATE_ATTR_ENTRIES_REMAINING = "remaining";

    private final FreeMarkerCache freemarker_;

    @Injectable
    public MarkdownDrivenContentResponseMapper(final FreeMarkerCache freemarker) {
        freemarker_ = freemarker;
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final MarkdownContent md) throws Exception {
        try {
            final Template tp = freemarker_.getConfig()
                .getTemplate(md.getTemplateName());
            try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
                final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
                tp.process(markdownContentToDataMap(md, tp), w);
                renderEntity(response, new Utf8CompressedHtmlEntity(os));
            }
        } catch (Exception e) {
            logger__.warn("Oops, content rendering exception: " + md, e);
        }
    }

    private static final Map<String,Object> markdownContentToDataMap(
        final MarkdownContent md, final Template tp) throws Exception {
        final Map<String,Object> map = Maps.newLinkedHashMap();
        final Object name = tp.getCustomAttribute(TEMPLATE_ATTR_NAME);
        map.put(TEMPLATE_ATTR_NAME, (name == null) ? md.getName() : name);
        final Object title = tp.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        map.put(TEMPLATE_ATTR_TITLE, (title == null) ? md.getTitle() : title);
        final Object hash = tp.getCustomAttribute(TEMPLATE_ATTR_COMMIT);
        map.put(TEMPLATE_ATTR_COMMIT, (hash == null) ? md.getCommit() : hash);
        final Object date = tp.getCustomAttribute(TEMPLATE_ATTR_DATE);
        map.put(TEMPLATE_ATTR_DATE, (date == null) ? md.getDateFormatted() : date);
        // Attach the Markdown content converted to a String to the data map.
        final String content;
        if((content = md.getContent()) != null) {
            map.put(TEMPLATE_ATTR_CONTENT, content);
        }
        // Only Index types get a list of entries and a remaining count
        // attached to their data map.
        if(md instanceof Index) {
            final Index idx = (Index)md;
            map.put(TEMPLATE_ATTR_ENTRIES, idx.getEntries());
            map.put(TEMPLATE_ATTR_ENTRIES_REMAINING, idx.getRemaining());
        }
        map.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        return map;
    }

    public static final String markdownToString(final MarkdownFile mdf)
        throws Exception {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(readFileToString(mdf.getFile(), Charsets.UTF_8));
    }

    public static final class Utf8CompressedHtmlEntity
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
