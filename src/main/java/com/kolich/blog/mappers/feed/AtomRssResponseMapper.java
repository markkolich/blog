package com.kolich.blog.mappers.feed;

import com.google.common.base.Charsets;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.feed.AtomRss;
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

import static com.kolich.blog.entities.html.Utf8XHtmlEntity.HtmlEntityType.XML;

@ControllerReturnTypeMapper(AtomRss.class)
public final class AtomRssResponseMapper
    extends AbstractFeedEntityResponseMapper<AtomRss> {

    private static final String ATOM_RSS_FEED_TEMPLATE_NAME = "feed/atom.ftl";

    @Injectable
    public AtomRssResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull AtomRss content) throws Exception {
        final Template tp = config_.getTemplate(ATOM_RSS_FEED_TEMPLATE_NAME);
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(getDataMap(tp, content), w);
            renderEntity(response, new Utf8XHtmlEntity(XML, os));
        }
    }

}
