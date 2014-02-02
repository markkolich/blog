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
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import javax.annotation.Nullable;
import java.io.File;

@Component
public final class EntryCache extends AbstractMarkdownCache<Entry> {

    private static final String entriesDir__ =
        ApplicationConfig.getEntriesDir();

    @Injectable
    public EntryCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Entry getEntity(final String name,
                                 final String title,
                                 final String commit,
                                 final Long timestamp,
                                 final File content) {
        return new Entry(name, title, commit, timestamp, content);
    }

    @Override
    public final String getCachedContentDirName() {
        return entriesDir__;
    }

    @Nullable
    public final Entry getEntry(final String name) {
        final Entry e;
        if((e = get(name)) == null) {
            throw new ContentNotFoundException("Failed to load entry for " +
                "key: " + name);
        }
        return e;
    }

    public final PagedContent<Entry> getEntries(final int limit) {
        return getAll(limit);
    }

    public final PagedContent<Entry> getEntriesBefore(@Nullable final String commit,
                                                      final int limit) {
        return getAllBefore(commit, limit);
    }

    public final AtomRss getAtomFeed(final int limit) {
        return new AtomRss(getAll(limit));
    }

    public final Sitemap getSitemap() {
        return new Sitemap(getAll());
    }

}
