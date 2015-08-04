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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public final class Entry extends MarkdownContent {

    private static final Pattern TAGS_REGEX = Pattern.compile("<!---\\s*tags:\\s*(.*?)-->",
        Pattern.CASE_INSENSITIVE);

    private static final Splitter TAGS_COMMA_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    private static final String ENTRY_TEMPLATE_NAME = "entry.ftl";

    public Entry(final String name,
                 final String title,
                 final String message,
                 final String commit,
                 final Long timestamp,
                 final File content) {
        super(ContentType.ENTRY, name, escapeHtml4(title), message, commit, timestamp, content);
    }

    public Entry(final String name,
                 final String title,
                 final String message,
                 final String commit,
                 final Long timestamp,
                 final String content) {
        this(name, title, message, commit, timestamp, new File(content));
    }

    @Override
    public final String getTemplateName() {
        return ENTRY_TEMPLATE_NAME;
    }

    /**
     * Returns the entry date formatted using the RFC3339 Atom/RSS timestamp format.
     */
    public final String getAtomFeedDateFormatted() {
        final Date date = getDate();
        return (date != null) ? AtomRss.AtomRssRFC3339DateFormat.format(date) : null;
    }

    /**
     * Returns the entry date formatted using the sitemap date format.
     */
    public final String getSitemapDateFormatted() {
        final Date date = getDate();
        return (date != null) ? Sitemap.SitemapDateFormat.format(date) : null;
    }

    /**
     * Returns an immutable list of all tags in the entry.  Will return an empty list when no tags
     * are present; guaranteed not to return null.
     */
    @Nonnull
    public final List<EntryTag> getTags() {
        final ImmutableList.Builder<EntryTag> tags = ImmutableList.builder();
        final String content = getContent(); // Converts the entry Markdown to HTML
        if (content != null) {
            // Find all tag comments/directives in the markdown content.
            final Matcher m = TAGS_REGEX.matcher(content);
            while (m.find()) {
                final String tagGroup = m.group(1);
                // Split each tag set on a comma, and add all to the resulting list; the splitter takes care of
                // empty strings and stripping off whitespace.
                final List<String> tagList = TAGS_COMMA_SPLITTER.splitToList(tagGroup);
                // Convert the list of extracted tags into their corresponding entry tag objects that allow things
                // like URL encoding.
                final List<EntryTag> entryTags = tagList.stream().map(EntryTag::new).collect(Collectors.toList());
                tags.addAll(entryTags);
            }
        }
        return tags.build();
    }

}
