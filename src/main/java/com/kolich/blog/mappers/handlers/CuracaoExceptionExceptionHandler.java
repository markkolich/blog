package com.kolich.blog.mappers.handlers;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerCache;
import com.kolich.blog.entities.html.Utf8CompressedHtmlEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.exceptions.CuracaoException;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import freemarker.template.Template;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(CuracaoException.class)
public final class CuracaoExceptionExceptionHandler
    extends RenderingResponseTypeMapper<CuracaoException> {

    private static final Logger logger__ =
        getLogger(CuracaoExceptionExceptionHandler.class);

    private static final String appContextPath__ =
        ApplicationConfig.getContextPath();

    private static final String TEMPLATE_ATTR_TITLE = "title";
    private static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    private final FreeMarkerCache freemarker_;

    @Injectable
    public CuracaoExceptionExceptionHandler(final FreeMarkerCache freemarker)
        throws IOException {
        freemarker_ = freemarker;
    }

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final CuracaoException exception) throws Exception {
        int status = SC_INTERNAL_SERVER_ERROR; // default
        if(exception instanceof CuracaoException.WithEntity) {
            status = ((CuracaoException.WithEntity)exception).getEntity().getStatus();
        }
        Template tp = null;
        try {
            tp = getErrorTemplateForStatus(status);
        } catch (Exception e) {
            // Loading the error template for the actual status code failed,
            // so let's load the default error template which should, in theory,
            // always exist.
            logger__.warn("Failed to load FreeMarker error page " +
                "template for HTTP status code: " + status, e);
            tp = getErrorTemplateForStatus(SC_INTERNAL_SERVER_ERROR);
            status = SC_INTERNAL_SERVER_ERROR;
        }
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(buildDataMap(tp), w);
            renderEntity(response, new Utf8CompressedHtmlEntity(status, os));
        }
    }

    private final Template getErrorTemplateForStatus(final int status)
        throws IOException {
        return freemarker_.getConfig().getTemplate(getTemplateName(status));
    }

    private final String getTemplateName(final int status) {
        return String.format("errors/%s.ftl", Integer.toString(status));
    }

    private static final Map<String,Object> buildDataMap(final Template tp)
        throws Exception {
        final Map<String,Object> map = Maps.newLinkedHashMap();
        final Object title = tp.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        map.put(TEMPLATE_ATTR_TITLE, title);
        map.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        return map;
    }

}
