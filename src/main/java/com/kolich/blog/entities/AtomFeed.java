package com.kolich.blog.entities;

import com.kolich.blog.entities.gson.PagedContent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.TimeZone.getTimeZone;

public final class AtomFeed {

    public static class AtomFeedRFC3339DateFormat {

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

    private final List<Entry> entries_;

    public AtomFeed(final List<Entry> entries) {
        entries_ = checkNotNull(entries, "Entries list cannot be null.");
    }

    public AtomFeed(final PagedContent<Entry> entries) {
        this(entries.getContent());
    }

    public final List<Entry> getEntries() {
        return entries_;
    }

}
