/**
 * Copyright (c) 2015 Mark S. Kolich
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

import com.kolich.blog.entities.html.Utf8TextEntity;
import curacao.entities.CuracaoEntity;
import curacao.entities.empty.StatusCodeOnlyCuracaoEntity;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.IF_NONE_MATCH;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

public abstract class AbstractETagAwareResponseMapper<T> extends AbstractDevModeSafeResponseMapper<T> {

    /**
     * We're using the "strong ETag" header format because the ETag we inject into the response headers
     * is a byte-for-byte SHA-1 hash of the rendered view response body.
     */
    private static final String STRONG_ETAG_HEADER_FORMAT = "\"%s\"";

    private static final CuracaoEntity NOT_MODIFIED_ENTITY = new StatusCodeOnlyCuracaoEntity(SC_NOT_MODIFIED);

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 @Nonnull final T entity) throws Exception {
        final Utf8TextEntity rendered = renderView(entity);
        // Only attach an ETag response header to the response if the rendered entity indicates success.
        // No ETag is sent back with error pages, like a "404 Not Found" response.
        if (rendered.getStatus() < SC_BAD_REQUEST) {
            final String ifNoneMatch = getIfNoneMatchFromRequest(context);
            // Compute the SHA-1 hash of the response body.
            final String sha1 = DigestUtils.sha1Hex(rendered.getBody());
            // Attach the SHA-1 hash of the response body to the outgoing response.
            final String eTag = String.format(STRONG_ETAG_HEADER_FORMAT, sha1);
            response.setHeader(ETAG, eTag); // Strong ETag!
            // If the If-None-Match header matches the computed SHA-1 hash of the rendered content, then we
            // send back "304 Not Modified" without a body.  If not, then send back the rendered content.
            if (eTag.equals(ifNoneMatch) || "*".equals(ifNoneMatch)) {
                // Return a 304. Done.
                renderEntity(response, NOT_MODIFIED_ENTITY);
                return;
            }
        }
        renderEntity(response, rendered);
    }

    public abstract Utf8TextEntity renderView(@Nonnull final T entity)
        throws Exception;

    private static String getIfNoneMatchFromRequest(final AsyncContext context) {
        final HttpServletRequest request = (HttpServletRequest)context.getRequest();
        return request.getHeader(IF_NONE_MATCH);
    }

}
