package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.FeedContent;
import com.kolich.blog.entities.html.Utf8XHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import freemarker.template.Template;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static com.kolich.blog.entities.html.Utf8XHtmlEntity.HtmlEntityType.XML;

@ControllerReturnTypeMapper(FeedContent.class)
public final class FeedContentResponseMapper
    extends AbstractFreeMarkerAwareResponseMapper<FeedContent> {

    @Injectable
    public FeedContentResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull FeedContent content) throws Exception {
        final Template tp = config_.getTemplate("feed/atom.ftl");
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(getDataMap(tp, content), w);
            renderEntity(response, new Utf8XHtmlEntity(XML, os));
        }
    }

    protected Map<String,Object> getDataMap(final Template tp,
                                            final FeedContent content) {
        final Map<String,Object> map = super.getDataMap(tp);
        map.put(TEMPLATE_ATTR_ENTRIES, content.getEntries());
        return map;
    }

}
