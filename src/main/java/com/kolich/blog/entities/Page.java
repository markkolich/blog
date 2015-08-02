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

import java.io.File;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

public final class Page extends MarkdownContent {

    private static final String PAGE_TEMPLATE_NAME = "page.ftl";

    public Page(final String name,
                final String title,
                final String message,
                final String commit,
                final Long timestamp,
                final File content) {
        super(ContentType.PAGE, name, escapeHtml4(title), message, commit, timestamp, content);
    }

    public Page(final String name,
                final String title,
                final String message,
                final String commit,
                final Long timestamp,
                final String content) {
        this(name, title, message, commit, timestamp, new File(content));
    }

    @Override
    public final String getTemplateName() {
        return PAGE_TEMPLATE_NAME;
    }

}
