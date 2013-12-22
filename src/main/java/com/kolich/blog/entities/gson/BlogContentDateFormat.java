package com.kolich.blog.entities.gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static java.util.TimeZone.getTimeZone;

public abstract class BlogContentDateFormat {

    private static final String GIT_DATE_FORMAT_STRING =
        "EEE dd MMM yyyy HH:mm:ss Z";

    private static final DateFormat format__;
    static {
        format__ = new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
        format__.setTimeZone(getTimeZone("GMT-8:00"));
    }

    // Cannot instantiate.
    private BlogContentDateFormat() {}

    public static final DateFormat getNewInstance() {
        return new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
    }

    public static final String getFormat() {
        return GIT_DATE_FORMAT_STRING;
    }

}
