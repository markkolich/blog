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

import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.html.Utf8TextEntity;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.entities.empty.StatusCodeOnlyCuracaoEntity;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.IF_NONE_MATCH;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

public abstract class AbstractFreeMarkerAwareResponseMapper<T>
    extends AbstractDevModeSafeResponseMapper<T> {

    /**
     * We're using the "strong ETag" header format because the ETag we
     * inject into the response headers is a byte-for-byte SHA-1 hash
     * of the rendered template body.
     */
    private static final String STRONG_ETAG_HEADER_FORMAT = "\"%s\"";

    private static final CuracaoEntity NOT_MODIFIED_ENTITY =
        new StatusCodeOnlyCuracaoEntity(SC_NOT_MODIFIED);

    protected static final String appContextPath__ =
        ApplicationConfig.getContextPath();
    protected static final String blogTitle__ =
        ApplicationConfig.getBlogTitle();
    protected static final String getBlogSubTitle__ =
        ApplicationConfig.getBlogSubTitle();
    protected static final String hostname__  =
        ApplicationConfig.getHostname();
    protected static final String fullUri__ =
        ApplicationConfig.getFullUri();

    protected static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    protected static final String TEMPLATE_ATTR_BLOG_TITLE = "blogTitle";
    protected static final String TEMPLATE_ATTR_BLOG_SUBTITLE = "blogSubTitle";
    protected static final String TEMPLATE_ATTR_HOSTNAME = "hostname";
    protected static final String TEMPLATE_ATTR_FULL_URI = "fullUri";

    protected static final String TEMPLATE_ATTR_NAME = "name";
    protected static final String TEMPLATE_ATTR_TITLE = "title";
    protected static final String TEMPLATE_ATTR_COMMIT = "commit";
    protected static final String TEMPLATE_ATTR_DATE = "date";
    protected static final String TEMPLATE_ATTR_CONTENT = "content";

    protected static final String TEMPLATE_ATTR_LAST_UPDATED = "lastUpdated";
    protected static final String TEMPLATE_ATTR_ENTRIES = "entries";
    protected static final String TEMPLATE_ATTR_ENTRIES_REMAINING = "remaining";

    protected final Configuration config_;

    public AbstractFreeMarkerAwareResponseMapper(final FreeMarkerConfig config) {
        config_ = config.getConfig();
    }

    protected Map<String,Object> getDataMap(final Template tp) {
        final Map<String,Object> map = Maps.newLinkedHashMap();
        map.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        map.put(TEMPLATE_ATTR_BLOG_TITLE, blogTitle__);
        map.put(TEMPLATE_ATTR_BLOG_SUBTITLE, getBlogSubTitle__);
        map.put(TEMPLATE_ATTR_HOSTNAME, hostname__);
        map.put(TEMPLATE_ATTR_FULL_URI, fullUri__);
        return map;
    }

    @Override
    public final void renderSafe(final AsyncContext context,
                                 final HttpServletResponse response,
                                 @Nonnull final T entity) throws Exception {
        final Utf8TextEntity rendered = renderTemplate(entity);
        // Only attach an ETag response header to the response if the
        // rendered entity indicates success.  No eTag is sent back for
        // error pages, like a "404 Not Found".
        if(rendered.getStatus() < SC_BAD_REQUEST) {
            final String ifNoneMatch = getIfNoneMatchFromRequest(context);
            final String sha1 = DigestUtils.sha1Hex(rendered.getBody());
            final String eTag = String.format(STRONG_ETAG_HEADER_FORMAT, sha1);
            response.setHeader(ETAG, eTag); // Strong ETag!
            // If the If-None-Match header matches the computed SHA-1 hash of
            // the rendered content, then we send back "304 Not Modified"
            // without a body.  If not, then send back the rendered content.
            if (eTag.equals(ifNoneMatch) || "*".equals(ifNoneMatch)) {
                // Return a 304, done.
                renderEntity(response, NOT_MODIFIED_ENTITY);
                return;
            }
        }
        renderEntity(response, rendered);
    }

    public abstract Utf8TextEntity renderTemplate(@Nonnull final T entity)
        throws Exception;

    private static String getIfNoneMatchFromRequest(final AsyncContext context) {
        final HttpServletRequest request = (HttpServletRequest)context
            .getRequest();
        return request.getHeader(IF_NONE_MATCH);
    }

}
