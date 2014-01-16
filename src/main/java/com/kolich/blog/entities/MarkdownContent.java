package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.TimeZone.getTimeZone;

public abstract class MarkdownContent {

    private static final String CLASS_SN =
        MarkdownContent.class.getSimpleName();

    public static enum ContentType {
        INDEX, ENTRY, PAGE;
    }

    public static class BlogContentDateFormat {

        private static final String GIT_DATE_FORMAT_STRING =
            "EEE dd MMM yyyy HH:mm:ss Z";

        public static final DateFormat getNewInstance() {
            final DateFormat df = new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
            df.setTimeZone(getTimeZone("GMT-8"));
            return df;
        }

        public static final String format(final Date d) {
            return getNewInstance().format(d);
        }

    }

    @SerializedName("type")
    private final ContentType type_;

    @SerializedName("name")
    private final String name_;

    @SerializedName("title")
    private final String title_;

    @SerializedName("commit")
    private final String commit_;

    @SerializedName("date")
    private final Date date_;

    @SerializedName("html")
    private final MarkdownFile content_;

    public MarkdownContent(final ContentType type,
                           final String name,
                           final String title,
                           final String commit,
                           final Date date,
                           final File content) {
        type_ = type;
        name_ = name;
        title_ = title;
        commit_ = commit;
        date_ = date;
        content_ = (content != null) ? new MarkdownFile(content) : null;
    }

    public MarkdownContent(final ContentType type,
                           final String name,
                           final String title,
                           final String commit,
                           final Date date) {
        this(type, name, title, commit, date, null);
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

    public final String getCommit() {
        return commit_;
    }

    public final Date getDate() {
        return date_;
    }
    public final String getDateFormatted() {
        final Date date = getDate();
        return (date != null) ? BlogContentDateFormat.format(date) : null;
    }

    public final MarkdownFile getMarkdownFile() {
        return content_;
    }
    public final String getContent() {
        String result = null;
        try {
            result = (content_ != null) ? content_.getHtmlFromMarkdown() : null;
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarkdownContent that = (MarkdownContent) o;
        if (name_ != null ? !name_.equals(that.name_) : that.name_ != null)
            return false;
        return true;
    }

    @Override
    public final int hashCode() {
        return (name_ != null) ? name_.hashCode() : 0;
    }

    @Override
    public final String toString() {
        return String.format("%s(name=\"%s\", title=\"%s\", file=\"%s\")",
            CLASS_SN, name_, title_, content_);
    }

    public abstract String getTemplateName();

}
