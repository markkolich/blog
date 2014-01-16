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
