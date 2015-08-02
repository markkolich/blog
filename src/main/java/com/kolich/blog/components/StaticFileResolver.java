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

package com.kolich.blog.components;

import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.exceptions.DirectoryListingException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import java.io.File;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public final class StaticFileResolver {

    private final GitRepository git_;

    @Injectable
    public StaticFileResolver(final GitRepository git) {
        git_ = checkNotNull(git, "Git repository cannot be null.");
    }

    public final File getStaticFileInContentRoot(final String uri) {
        final File file = new File(git_.getContentRoot(),
            Paths.get(uri).toString());
        if(!file.exists()) {
            throw new ContentNotFoundException("Could not find static resource file: " + file.getAbsolutePath());
        } else if(file.isDirectory()) {
            throw new DirectoryListingException("Will not list contents of directory: " + file.getAbsolutePath());
        }
        return file;
    }

}
