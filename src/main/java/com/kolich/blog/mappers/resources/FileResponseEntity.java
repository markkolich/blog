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

package com.kolich.blog.mappers.resources;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.google.common.primitives.Ints;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.kolich.blog.ApplicationConfig.getContentTypeForExtension;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.IOUtils.copyLarge;

public final class FileResponseEntity extends UnmodifiableCacheableEntity {

    private static final String DEFAULT_CONTENT_TYPE =
        MediaType.OCTET_STREAM.toString();

    public FileResponseEntity(final HttpServletResponse response,
                              final File file,
                              final String eTag) {
        super(response, file, eTag);
    }

    @Override
    public final int getStatus() {
        return SC_OK;
    }

    @Override
    public final String getContentType() {
        // Normalize (important) and extract the extension from the file.
        final String extension = getExtension(normalize(
            file_.getAbsolutePath()));
        final String contentType = getContentTypeForExtension(extension,
            DEFAULT_CONTENT_TYPE);
        // Build a proper MediaType object representing the files true
        // Content-Type.  It really annoys me that 'contentType' below
        // isn't final, but it has to be this way given we need to set
        // the "charset=UTF-8" attribute on the type later if it's text.
        MediaType mediaType = MediaType.parse(contentType);
        // Is it text?  If so, we need to a proper UTF-8 charset attribute.
        if (isText(mediaType)) {
            mediaType = mediaType.withCharset(Charsets.UTF_8);
        }
        return mediaType.toString();
    }

    @Override
    public final void writeAfterHeaders(final OutputStream os) throws Exception {
        // Sigh.  Yep, here we are "safely" casting a Long to an Integer.
        // This ~should~ be fine, in theory, given that this response
        // mapper is likely not going to serve up content larger than
        // 2GB.  If we do ever need to serve up crazy large files, this
        // will not work.  By that time, we'll probably be using Servlet
        // 3.1 which has a proper setContentLength(Long) method.
        response_.setContentLength(Ints.checkedCast(file_.length()));
        try(final InputStream is = new FileInputStream(file_)) {
            copyLarge(is, os);
        }
    }

    /**
     * Returns true if the provided {@link MediaType} is "text", and false
     * otherwise.  More formally, if the provided {@link MediaType}'s type is
     * text, ignoring its subtype. Note that this method also returns true for
     * types that we want to be UTF-8, like JavaScript and JSON, but their
     * formal {@link MediaType} type is not "text".
     */
    private static final boolean isText(@Nonnull final MediaType type) {
        return type.is(MediaType.ANY_TEXT_TYPE) ||
            // JavaScript and JSON needs to be served up as "UTF-8" too.
            // Apparently rumor has it JSON (and JavaScript?) is considered
            // by most modern browsers to be UTF-8 by default, so this
            // special handling of "js" and "json" types might be unnecessary.
            // IDK, it certainly can't hurt and bytes are cheap.
            type.subtype().equals(MediaType.JAVASCRIPT_UTF_8.subtype()) ||
            type.subtype().equals(MediaType.JSON_UTF_8.subtype());
    }

}
