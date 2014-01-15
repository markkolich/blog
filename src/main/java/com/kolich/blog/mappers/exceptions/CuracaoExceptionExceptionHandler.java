package com.kolich.blog.mappers.exceptions;

import com.google.common.base.Charsets;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.html.Utf8XHtmlEntity;
import com.kolich.blog.mappers.AbstractFreeMarkerAwareResponseMapper;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.exceptions.CuracaoException;
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

import static com.kolich.blog.entities.html.Utf8XHtmlEntity.HtmlEntityType.HTML;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(CuracaoException.class)
public final class CuracaoExceptionExceptionHandler
    extends AbstractFreeMarkerAwareResponseMapper<CuracaoException> {

    private static final Logger logger__ =
        getLogger(CuracaoExceptionExceptionHandler.class);

    private static final int DEFAULT_ERROR_STATUS_CODE =
        SC_INTERNAL_SERVER_ERROR;

    @Injectable
    public CuracaoExceptionExceptionHandler(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 @Nonnull final CuracaoException exception) throws Exception {
        int status = DEFAULT_ERROR_STATUS_CODE; // initialized to default
        // If the incoming exception to handle is an instance of a type that
        // contains a status code (e.g., with entity or with status) then we
        // extract the status code from the entity backed exception and use
        // that with the error response.
        if(exception instanceof CuracaoException.WithEntity) {
            final CuracaoEntity entity;
            if((entity = ((CuracaoException.WithEntity)exception).getEntity())
                != null) {
                status = entity.getStatus();
            }
        }
        // Attempt to load the error page template associated with the resulting
        // status code, if one exists.  The template may not exist, in which
        // case we fall back to the default to render a generic 500 error page.
        Template tp = null;
        try {
            tp = getErrorTemplateForStatus(status);
        } catch (Exception e) {
            // Loading the error template for the actual status code failed,
            // so let's load the default error template which should, in theory,
            // always exist.
            logger__.warn("Failed to load FreeMarker error page " +
                "template for HTTP status code: " + status, e);
            // Use default!  This ~should~ not fail given that the 500 error
            // page template must exist... if it doesn't then something else
            // is wrong.
            tp = getErrorTemplateForStatus(SC_INTERNAL_SERVER_ERROR);
            status = SC_INTERNAL_SERVER_ERROR;
        }
        // Write the response.
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8);) {
            tp.process(getDataMap(tp), w);
            renderEntity(response, new Utf8XHtmlEntity(HTML, status, os));
        }
    }

    private final Template getErrorTemplateForStatus(final int status)
        throws IOException {
        return config_.getTemplate(getTemplateName(status));
    }

    private final String getTemplateName(final int status) {
        return String.format("errors/%s.ftl", Integer.toString(status));
    }

    @Override
    protected final Map<String,Object> getDataMap(final Template tp) {
        final Map<String,Object> map = super.getDataMap(tp);
        final Object title = tp.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        map.put(TEMPLATE_ATTR_TITLE, title);
        return map;
    }

}
