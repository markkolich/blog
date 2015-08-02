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

import com.kolich.blog.entities.gson.PagedContent;

import java.util.List;

public final class Tagged extends MarkdownContent {

    private static final String TAGGED_TEMPLATE_NAME = "tagged.ftl";

    private final String tag_;
    private final PagedContent<Entry> entries_;

    public Tagged(final String tag,
                  final PagedContent<Entry> entries) {
        super(ContentType.TAGGED, null, null, null, null, null);
        tag_ = tag;
        entries_ = entries;
    }

    public final String getTagDisplayText() {
        return EntryTag.encodeAndEscapeTag(tag_);
    }

    public final List<Entry> getEntries() {
        return entries_.getContent();
    }

    @Override
    public final String getTemplateName() {
        return TAGGED_TEMPLATE_NAME;
    }

}
