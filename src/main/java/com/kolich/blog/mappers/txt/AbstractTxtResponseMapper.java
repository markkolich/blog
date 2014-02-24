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

package com.kolich.blog.mappers.txt;

import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.mappers.AbstractFreeMarkerAwareResponseMapper;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import freemarker.template.Template;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;

import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public abstract class AbstractTxtResponseMapper<T>
    extends AbstractFreeMarkerAwareResponseMapper<T> {

    private static final String PLAIN_TEXT_CONTENT_TYPE_STRING =
        PLAIN_TEXT_UTF_8.toString();

    public AbstractTxtResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 final @Nonnull T entity) throws Exception {
        final Template tp = config_.getTemplate(getTemplateName());
        renderEntity(response, new AppendableCuracaoEntity() {
            @Override
            public final void toWriter(final Writer writer) throws Exception {
                tp.process(getDataMap(tp), writer);
            }
            @Override
            public final int getStatus() {
                return SC_OK;
            }
            @Override
            public final String getContentType() {
                return PLAIN_TEXT_CONTENT_TYPE_STRING;
            }
        });
    }

    public abstract String getTemplateName();

}
