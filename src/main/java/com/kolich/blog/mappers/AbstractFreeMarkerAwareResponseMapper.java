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

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.html.Utf8TextEntity;
import com.kolich.blog.entities.html.Utf8TextEntity.TextEntityType;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_OK;

public abstract class AbstractFreeMarkerAwareResponseMapper<T>
    extends AbstractETagAwareResponseMapper<T> {

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

    private final Configuration config_;

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

    protected final Template getTemplate(final String templateName)
        throws IOException {
        return config_.getTemplate(templateName);
    }

    /**
     * Given a template, a data map for the template, and the resulting
     * content/media type, renders the template and returns a new text
     * entity representing the typed response body.
     */
    protected static final Utf8TextEntity buildEntity(final Template tp,
                                                      final Map<String, Object> dataMap,
                                                      final TextEntityType type,
                                                      final int status) throws Exception {
        try(final ByteArrayOutputStream os = new ByteArrayOutputStream();
            final Writer w = new OutputStreamWriter(os, Charsets.UTF_8)) {
            tp.process(dataMap, w);
            return new Utf8TextEntity(type, status, os);
        }
    }

    /**
     * Same as the above static method, but defaults to a "200 OK" response.
     */
    protected static final Utf8TextEntity buildEntity(final Template tp,
                                                      final Map<String, Object> dataMap,
                                                      final TextEntityType type) throws Exception {
        return buildEntity(tp, dataMap, type, SC_OK);
    }

}
