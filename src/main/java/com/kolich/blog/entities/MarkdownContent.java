package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;
import com.kolich.blog.entities.gson.BlogContentDateFormat;

import java.io.File;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MarkdownContent {

    private static final String CLASS_SN =
        MarkdownContent.class.getSimpleName();

    public static enum ContentType {
        INDEX, ENTRY, PAGE;
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
        type_ = checkNotNull(type, "Content type cannot be null.");
        name_ = checkNotNull(name, "Content name cannot be null.");
        title_ = checkNotNull(title, "Content title cannot be null.");
        hash_ = checkNotNull(hash, "Git commit hash cannot be null.");
        date_ = checkNotNull(date, "Commit date/timestamp cannot be null.");
        content_ = new MarkdownFile(content);
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
        return BlogContentDateFormat.getNewInstance().format(date_);
    }

    public final MarkdownFile getContent() {
        return content_;
    }

    @Override
    public final String toString() {
        return String.format("%s(%s, %s, %s)", CLASS_SN, name_, title_, content_);
    }

    public abstract String getTemplateName();

}
