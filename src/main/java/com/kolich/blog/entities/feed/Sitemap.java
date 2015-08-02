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

package com.kolich.blog.entities.feed;

import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.gson.PagedContent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.util.TimeZone.getTimeZone;

public final class Sitemap extends AbstractFeedEntity {

    public static class SitemapDateFormat {

        private static final String SITEMAP_DATE_FORMAT_STRING = "yyyy-MM-dd";

        public static final DateFormat getNewInstance() {
            final DateFormat df = new SimpleDateFormat(SITEMAP_DATE_FORMAT_STRING);
            df.setTimeZone(getTimeZone("GMT-0"));
            return df;
        }

        public static final String format(final Date d) {
            return getNewInstance().format(d);
        }

    }

    public Sitemap(final List<Entry> entries) {
        super(entries);
    }

    public Sitemap(final PagedContent<Entry> entries) {
        this(entries.getContent());
    }

}
