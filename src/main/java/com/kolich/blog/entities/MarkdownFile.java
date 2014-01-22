/**
 * Copyright (c) 2014 Mark S. Kolich
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

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.kolich.blog.exceptions.ContentRenderException;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FileUtils.readFileToString;

public final class MarkdownFile {

    private final transient File file_;

    public MarkdownFile(final File file) {
        file_ = checkNotNull(file, "Markdown file cannot be null.");
    }

    public final File getFile() {
        return file_;
    }

    public final String getHtmlFromMarkdown() throws IOException {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(readFileToString(file_, Charsets.UTF_8));
    }

    @Override
    public final String toString() {
        return file_.getAbsolutePath();
    }

    public static final class MarkdownFileGsonAdapter
        implements JsonSerializer<MarkdownFile> {

        @Override
        public final JsonElement serialize(final MarkdownFile src,
                                           final Type typeOfSrc,
                                           final JsonSerializationContext context) {
            try {
                return new JsonPrimitive(src.getHtmlFromMarkdown());
            } catch (Exception e) {
                throw new ContentRenderException("Failed to serialize " +
                    "markdown file: " + src.getFile(), e);
            }
        }

    }

}
