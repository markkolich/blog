package com.kolich.blog.mappers.feed;

import com.google.common.base.Charsets;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.feed.Sitemap;
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

@ControllerReturnTypeMapper(Sitemap.class)
public final class SitemapResponseMapper
    extends AbstractFeedEntityResponseMapper<Sitemap> {

    private static final String SITEMAP_TEMPLATE_NAME = "feed/sitemap.ftl";

    @Injectable
    public SitemapResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull Sitemap sitemap) throws Exception {
        final Template tp = config_.getTemplate(SITEMAP_TEMPLATE_NAME);
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(getDataMap(tp, sitemap), w);
            renderEntity(response, new Utf8XHtmlEntity(XML, os));
        }
    }

}
