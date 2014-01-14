package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerCache;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.MarkdownFile;
import com.kolich.blog.entities.html.Utf8CompressedHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(MarkdownContent.class)
public final class MarkdownDrivenContentResponseMapper
    extends AbstractDevModeSafeResponseMapper<MarkdownContent> {

    private static final Logger logger__ =
        getLogger(MarkdownDrivenContentResponseMapper.class);

    private static final String appContextPath__ =
        ApplicationConfig.getContextPath();
    private static final String blogTitle__ =
        ApplicationConfig.getBlogTitle();
    private static final String getBlogSubTitle__ =
        ApplicationConfig.getBlogSubTitle();
    private static final String hostname__  =
        ApplicationConfig.getHostname();
    private static final String fullUri__ =
        ApplicationConfig.getFullUri();

    private static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    private static final String TEMPLATE_ATTR_BLOG_TITLE = "blogTitle";
    private static final String TEMPLATE_ATTR_BLOG_SUBTITLE = "blogSubTitle";
    private static final String TEMPLATE_ATTR_HOSTNAME = "hostname";
    private static final String TEMPLATE_ATTR_FULL_URI = "fullUri";

    private static final String TEMPLATE_ATTR_NAME = "name";
    private static final String TEMPLATE_ATTR_TITLE = "title";
    private static final String TEMPLATE_ATTR_COMMIT = "commit";
    private static final String TEMPLATE_ATTR_DATE = "date";
    private static final String TEMPLATE_ATTR_CONTENT = "content";

    private static final String TEMPLATE_ATTR_ENTRIES = "entries";
    private static final String TEMPLATE_ATTR_ENTRIES_REMAINING = "remaining";

    private final Configuration fmConfig_;

    @Injectable
    public MarkdownDrivenContentResponseMapper(final FreeMarkerCache freemarker) {
        fmConfig_= freemarker.getConfig();
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 @Nonnull final MarkdownContent md) throws Exception {
        try {
            final Template tp = fmConfig_.getTemplate(md.getTemplateName());
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
        map.put(TEMPLATE_ATTR_BLOG_TITLE, blogTitle__);
        map.put(TEMPLATE_ATTR_BLOG_SUBTITLE, getBlogSubTitle__);
        map.put(TEMPLATE_ATTR_HOSTNAME, hostname__);
        map.put(TEMPLATE_ATTR_FULL_URI, fullUri__);
        return map;
    }

    public static final String markdownToString(final MarkdownFile mdf)
        throws Exception {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(readFileToString(mdf.getFile(), Charsets.UTF_8));
    }

}
