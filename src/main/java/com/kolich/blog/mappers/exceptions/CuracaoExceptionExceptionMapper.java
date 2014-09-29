/**
 * Copyright (c) 2014 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.blog.mappers.exceptions;

import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.html.Utf8TextEntity;
import com.kolich.blog.mappers.AbstractFreeMarkerAwareResponseMapper;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.exceptions.CuracaoException;
import freemarker.template.Template;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import static com.kolich.blog.entities.html.Utf8TextEntity.TextEntityType.HTML;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(CuracaoException.class)
public final class CuracaoExceptionExceptionMapper
    extends AbstractFreeMarkerAwareResponseMapper<CuracaoException> {

    private static final Logger logger__ =
        getLogger(CuracaoExceptionExceptionMapper.class);

    private static final String ERROR_TEMPLATE_FREEMARKER_PATH =
        "errors/%s.ftl";

    private static final int DEFAULT_ERROR_STATUS_CODE =
        SC_INTERNAL_SERVER_ERROR;

    @Injectable
    public CuracaoExceptionExceptionMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final Utf8TextEntity renderView(@Nonnull final CuracaoException e)
        throws Exception {
        int status = DEFAULT_ERROR_STATUS_CODE; // initialized to default
        // If the incoming exception to handle is an instance of a type that
        // contains a status code (e.g., with entity or with status) then we
        // extract the status code from the entity backed exception and use
        // that with the error response.
        if(e instanceof CuracaoException.WithEntity) {
            final CuracaoEntity entity;
            if((entity = ((CuracaoException.WithEntity)e).getEntity())
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
        } catch (Exception f) {
            // Loading the error template for the actual status code failed,
            // so let's load the default error template which should, in theory,
            // always exist.
            logger__.warn("Failed to load FreeMarker error page " +
                "template for HTTP status code: " + status, f);
            // Use default!  This ~should~ not fail given that the 500 error
            // page template must exist... if it doesn't then something else
            // is wrong.
            tp = getErrorTemplateForStatus(SC_INTERNAL_SERVER_ERROR);
            status = SC_INTERNAL_SERVER_ERROR;
        }
        return buildEntity(tp, getDataMap(tp), HTML, status);
    }

    private final Template getErrorTemplateForStatus(final int status)
        throws IOException {
        return getTemplate(getTemplateName(status));
    }

    private final String getTemplateName(final int status) {
        return String.format(ERROR_TEMPLATE_FREEMARKER_PATH,
            Integer.toString(status));
    }

    @Override
    protected final Map<String,Object> getDataMap(final Template tp) {
        final Map<String,Object> map = super.getDataMap(tp);
        final Object title = tp.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        map.put(TEMPLATE_ATTR_TITLE, title);
        return map;
    }

}
