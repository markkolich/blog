package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.FeedContent;
import com.kolich.blog.entities.html.Utf8XHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import freemarker.template.Configuration;
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
    extends AbstractDevModeSafeResponseMapper<FeedContent> {

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

    private static final String TEMPLATE_ATTR_BLOG_TITLE = "blogTitle";
    private static final String TEMPLATE_ATTR_BLOG_SUBTITLE = "blogSubTitle";

    private static final String TEMPLATE_ATTR_ENTRIES = "entries";
    private static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";
    private static final String TEMPLATE_ATTR_HOSTNAME = "hostname";
    private static final String TEMPLATE_ATTR_FULL_URI = "fullUri";

    private final Configuration fmConfig_;

    @Injectable
    public FeedContentResponseMapper(final FreeMarkerConfig freemarker) {
        fmConfig_ = freemarker.getConfig();
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull FeedContent content) throws Exception {
        final Template tp = fmConfig_.getTemplate("feed/atom.ftl");
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(buildDataMap(content), w);
            renderEntity(response, new Utf8XHtmlEntity(XML, os));
        }
    }

    private static final Map<String,Object> buildDataMap(
        final FeedContent content) {
        final Map<String,Object> map = Maps.newLinkedHashMap();
        map.put(TEMPLATE_ATTR_ENTRIES, content.getEntries());
        map.put(TEMPLATE_ATTR_BLOG_TITLE, blogTitle__);
        map.put(TEMPLATE_ATTR_BLOG_SUBTITLE, getBlogSubTitle__);
        map.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        map.put(TEMPLATE_ATTR_HOSTNAME, hostname__);
        map.put(TEMPLATE_ATTR_FULL_URI, fullUri__);
        return map;
    }

}
