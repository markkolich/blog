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

package com.kolich.blog.components.cache;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Page;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

@Component
public final class PageCache extends AbstractMarkdownCache<Page> {

    private static final String pagesDir__ =
        ApplicationConfig.getPagesDir();

    @Injectable
    public PageCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Page getEntity(final String name,
                                final String title,
                                final String commit,
                                final Date date,
                                final File content) {
        return new Page(name, title, commit, date, content);
    }

    @Override
    public final String getCachedContentDirName() {
        return pagesDir__;
    }

    @Nullable
    public final Page getPage(final String name) {
        final Page p;
        if((p = get(name)) == null) {
            throw new ContentNotFoundException("Failed to load page for " +
                "key: " + name);
        }
        return p;
    }

}
