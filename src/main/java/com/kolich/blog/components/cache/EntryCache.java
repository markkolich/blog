package com.kolich.blog.components.cache;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.AtomFeed;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

@Component
public final class EntryCache extends MarkdownCacheComponent<Entry> {

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
                                 final Date date,
                                 final File content) {
        return new Entry(name, title, commit, date, content);
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

    public final AtomFeed getFeedEntries(final int limit) {
        return new AtomFeed(getAll(limit));
    }

}
