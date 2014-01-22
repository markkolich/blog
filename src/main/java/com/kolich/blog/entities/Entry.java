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

package com.kolich.blog.entities;

import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;

import java.io.File;
import java.util.Date;

public final class Entry extends MarkdownContent {

    private static final String TEMPLATE_NAME = "entry.ftl";

    public Entry(final String name,
                 final String title,
                 final String commit,
                 final Date date,
                 final File content) {
        super(ContentType.ENTRY, name, title, commit, date, content);
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

    /**
     * Returns the entry date formatted using the RFC3339 Atom/RSS
     * timestamp format.
     */
    public final String getAtomFeedDateFormatted() {
        final Date date = getDate();
        return (date != null) ?
            AtomRss.AtomRssRFC3339DateFormat.format(date) :
            null;
    }

    /**
     * Returns the entry date formatted using the sitemap date format.
     */
    public final String getSitemapDateFormatted() {
        final Date date = getDate();
        return (date != null) ?
            Sitemap.SitemapDateFormat.format(date) :
            null;
    }

}
