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

package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;
import com.kolich.blog.ApplicationConfig;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.TimeZone.getTimeZone;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class MarkdownContent {

    private static final Logger logger__ = getLogger(MarkdownContent.class);

    private static final String CLASS_SN = MarkdownContent.class.getSimpleName();

    public enum ContentType {
        INDEX, ENTRY, PAGE, TAGGED
    }

    public static final class BlogContentDateFormat {

        private static final String TIME_ZONE_ID = ApplicationConfig.getTimeZone();

        /**
         * The default date format string used in Git.  Produces a date that looks like:
         * Sat Sep 12 16:37:41 2015 -0700
         */
        private static final String GIT_DATE_FORMAT_STRING = "EEE MMM dd HH:mm:ss yyyy Z";

        public static final DateFormat getNewInstance() {
            final DateFormat df = new SimpleDateFormat(GIT_DATE_FORMAT_STRING);
            df.setTimeZone(getTimeZone(TIME_ZONE_ID)); // US-Pacific time
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

    @SerializedName("message")
    private final String message_;

    @SerializedName("commit")
    private final String commit_;

    @SerializedName("date")
    private final Date date_;

    @SerializedName("html")
    private final MarkdownFile content_;

    public MarkdownContent(@Nonnull final ContentType type,
                           @Nullable final String name,
                           @Nullable final String title,
                           @Nullable final String message,
                           @Nullable final String commit,
                           @Nullable final Long commitTime,
                           @Nullable final File content) {
        type_ = checkNotNull(type, "Markdown content type cannot be null.");
        name_ = name;
        title_ = escapeHtml4(title);
        message_ = message;
        commit_ = commit;
        date_ = (commitTime != null) ? new Date(commitTime) : null;
        content_ = (content != null) ? new MarkdownFile(content) : null;
    }

    public MarkdownContent(@Nonnull final ContentType type,
                           @Nullable final String name,
                           @Nullable final String title,
                           @Nullable final String message,
                           @Nullable final String commit,
                           @Nullable final Long commitTime) {
        this(type, name, title, message, commit, commitTime, null);
    }

    @Nonnull
    public final ContentType getType() {
        return type_;
    }

    @Nullable
    public final String getName() {
        return name_;
    }

    @Nullable
    public final String getTitle() {
        return title_;
    }

    @Nullable
    public final String getMessage() {
        return message_;
    }

    @Nullable
    public final String getCommit() {
        return commit_;
    }

    @Nullable
    public final Date getDate() {
        return (date_ != null) ? new Date(date_.getTime()) : null;
    }
    @Nullable
    public final String getDateFormatted() {
        final Date date = getDate();
        return (date != null) ? BlogContentDateFormat.format(date) : null;
    }

    public final MarkdownFile getMarkdownFile() {
        return content_;
    }

    /**
     * Returns the content in its rendered HTML form.  May return null if this entity has no
     * associated content, or HTML could not be generated for some reason.
     */
    @Nullable
    public final String getContent() {
        String result = null;
        try {
            result = (content_ != null) ? content_.getHtmlFromMarkdown() : null;
        } catch (Exception e) {
            logger__.warn("Failed to generate HTML from Markdown content: {}", toString(), e);
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
        return String.format("%s(name=\"%s\", title=\"%s\", file=\"%s\", commit=\"%s\")",
            CLASS_SN, name_, title_, content_, commit_);
    }

    public abstract String getTemplateName();

}
