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

import com.kolich.curacao.entities.CuracaoEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;

import static com.google.common.net.HttpHeaders.ETAG;
import static com.google.common.net.HttpHeaders.LAST_MODIFIED;

public abstract class UnmodifiableCacheableEntity implements CuracaoEntity {

    // Must be sure to send the ETag HTTP response header if one would be sent
    // with a 200 OK.  This apparently has implications for Apache's
    // "mod_cache" which breaks if you omit it.  E.g., mod_cache will send
    // Gzip'ed content but forgets the "Content-Encoding: gzip" header which
    // breaks clients.
    // https://bugzilla.redhat.com/show_bug.cgi?id=845532
    // https://issues.apache.org/bugzilla/show_bug.cgi?id=52120

    protected final HttpServletResponse response_;
    protected final File file_;
    protected final String eTag_;

    public UnmodifiableCacheableEntity(final HttpServletResponse response,
                                       final File file,
                                       final String eTag) {
        response_ = response;
        file_ = file;
        eTag_ = eTag;
    }

    @Override
    public final void write(final OutputStream os) throws Exception {
        // Set the HTTP ETag defining some reasonable "weak" ETag for browsers.
        response_.setHeader(ETAG, eTag_);
        // Also set the Last-Modified header for good-measure.
        response_.setDateHeader(LAST_MODIFIED, file_.lastModified());
        writeAfterHeaders(os);
    }

    public abstract void writeAfterHeaders(final OutputStream os)
        throws Exception;

}
