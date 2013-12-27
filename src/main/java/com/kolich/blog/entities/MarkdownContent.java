package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.kolich.blog.mappers.MarkdownDrivenContentResponseMapper.markdownToString;
import static java.util.TimeZone.getTimeZone;

public abstract class MarkdownContent {

    private static final String CLASS_SN =
        MarkdownContent.class.getSimpleName();

    public static enum ContentType {
        INDEX, ENTRY, PAGE;
    }

    private static class BlogContentDateFormat {

        private static final String GIT_DATE_FORMAT_STRING =
            "EEE dd MMM yyyy HH:mm:ss Z";

        private static final DateFormat format__;
        static {
            format__ = new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
            format__.setTimeZone(getTimeZone("GMT-8:00"));
        }

        public static final DateFormat getNewInstance() {
            return new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
        }

    }

    @SerializedName("type")
    private final ContentType type_;

    @SerializedName("name")
    private final String name_;

    @SerializedName("title")
    private final String title_;

    @SerializedName("commit")
    private final String hash_;

    @SerializedName("date")
    private final Date date_;

    @SerializedName("html")
    private final MarkdownFile content_;

    public MarkdownContent(final ContentType type,
                           final String name,
                           final String title,
                           final String hash,
                           final Date date,
                           final File content) {
        type_ = type;
        name_ = name;
        title_ = title;
        hash_ = hash;
        date_ = date;
        content_ = (content != null) ? new MarkdownFile(content) : null;
    }

    public final ContentType getType() {
        return type_;
    }

    public final String getName() {
        return name_;
    }

    public final String getTitle() {
        return title_;
    }

    public final String getHash() {
        return hash_;
    }

    public final Date getDate() {
        return date_;
    }
    public final String getDateFormatted() {
        return (date_ != null) ? BlogContentDateFormat.getNewInstance().format(date_) : null;
    }

    public final MarkdownFile getMarkdownFile() {
        return content_;
    }
    public final String getContent() {
        String result = null;
        try {
            result = (content_ != null) ? markdownToString(content_) : null;
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    @Override
    public final String toString() {
        return String.format("%s(%s, %s, %s)", CLASS_SN, name_, title_, content_);
    }

    public abstract String getTemplateName();

}
