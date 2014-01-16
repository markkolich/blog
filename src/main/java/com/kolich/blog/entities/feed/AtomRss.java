package com.kolich.blog.entities.feed;

import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.gson.PagedContent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.util.TimeZone.getTimeZone;

public final class AtomRss extends AbstractFeedEntity {

    public static class AtomRssRFC3339DateFormat {

        private static final String RFC3339_DATE_FORMAT_STRING =
            "yyyy-MM-dd'T'HH:mm:ss'Z'";

        public static final DateFormat getNewInstance() {
            final DateFormat df = new SimpleDateFormat(
                RFC3339_DATE_FORMAT_STRING);
            df.setTimeZone(getTimeZone("GMT-0"));
            return df;
        }

        public static final String format(final Date d) {
            return getNewInstance().format(d);
        }

    }

    public AtomRss(final List<Entry> entries) {
        super(entries);
    }

    public AtomRss(final PagedContent<Entry> entries) {
        this(entries.getContent());
    }

}
