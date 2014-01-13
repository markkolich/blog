package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerCache;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.FeedContent;
import com.kolich.blog.entities.html.Utf8XmlEntity;
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
import java.util.List;
import java.util.Map;

@ControllerReturnTypeMapper(FeedContent.class)
public final class FeedContentResponseMapper
    extends AbstractDevModeSafeResponseMapper<FeedContent> {

    private static final String appContextPath__ =
        ApplicationConfig.getContextPath();

    private static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    private final Configuration fmConfig_;

    @Injectable
    public FeedContentResponseMapper(final FreeMarkerCache freemarker) {
        fmConfig_ = freemarker.getConfig();
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull FeedContent content) throws Exception {
        final List<Entry> entries = content.getEntries();
        final Template tp = fmConfig_.getTemplate("feed/atom.ftl");
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(buildDataMap(tp), w);
            renderEntity(response, new Utf8XmlEntity(os));
        }
    }

    private static final Map<String,Object> buildDataMap(final Template tp)
        throws Exception {
        return ImmutableMap.<String,Object>of(TEMPLATE_ATTR_CONTEXT_PATH,
            appContextPath__);
    }

}
