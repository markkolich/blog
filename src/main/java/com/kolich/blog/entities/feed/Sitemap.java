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

        private static final String SITEMAP_DATE_FORMAT_STRING =
            "yyyy-MM-dd";

        public static final DateFormat getNewInstance() {
            final DateFormat df = new SimpleDateFormat(
                SITEMAP_DATE_FORMAT_STRING);
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
