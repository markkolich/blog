package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.html.Utf8XHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import freemarker.template.Template;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static com.kolich.blog.entities.html.Utf8XHtmlEntity.HtmlEntityType.HTML;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(MarkdownContent.class)
public final class MarkdownDrivenContentResponseMapper
    extends AbstractFreeMarkerAwareResponseMapper<MarkdownContent> {

    private static final Logger logger__ =
        getLogger(MarkdownDrivenContentResponseMapper.class);

    @Injectable
    public MarkdownDrivenContentResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 @Nonnull final MarkdownContent md) throws Exception {
        try {
            final Template tp = config_.getTemplate(md.getTemplateName());
            try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
                final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
                tp.process(getDataMap(tp, md), w);
                renderEntity(response, new Utf8XHtmlEntity(HTML, os));
            }
        } catch (Exception e) {
            logger__.warn("Oops, content rendering exception: " + md, e);
        }
    }

    protected Map<String,Object> getDataMap(final Template tp,
                                            final MarkdownContent md) {
        final Map<String,Object> map = super.getDataMap(tp);
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
        return map;
    }

}
