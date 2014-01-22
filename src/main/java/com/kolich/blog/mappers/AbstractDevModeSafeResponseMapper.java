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

package com.kolich.blog.mappers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;

public abstract class AbstractDevModeSafeResponseMapper<T>
    extends RenderingResponseTypeMapper<T> {

    private static final boolean isDevMode__ = ApplicationConfig.isDevMode();

    private static final String CACHE_CONTROL_NO_CACHE =
        "no-store, no-cache, must-revalidate, post-check=0, pre-check=0";

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final T content) throws Exception {
        // When in 'development' mode we set this header to prevent
        // any browser or proxy caching of CSS, JavaScript, or images.
        if(isDevMode__) {
            response.addHeader(CACHE_CONTROL, CACHE_CONTROL_NO_CACHE);
        }
        renderSafe(context, response, content);
    }

    public abstract void renderSafe(final AsyncContext context,
                                    final HttpServletResponse response,
                                    @Nonnull final T content) throws Exception;

}
