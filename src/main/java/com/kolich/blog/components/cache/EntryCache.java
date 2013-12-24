package com.kolich.blog.components.cache;

import com.google.common.collect.Sets;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.EntryList;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;
import java.util.Set;

@Component
public final class EntryCache extends MarkdownCacheComponent<Entry> {

    private static final String ENTRIES_CONTENT_DIR_NAME = "entries";

    @Injectable
    public EntryCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Entry getEntity(final String name,
                                 final String title,
                                 final String hash,
                                 final Date date,
                                 final File content) {
        return new Entry(name, title, hash, date, content);
    }

    @Override
    public final String getContentDirectoryName() {
        return ENTRIES_CONTENT_DIR_NAME;
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

    public final Set<Entry> getEntries() {
        return Sets.newLinkedHashSet(getAll().values());
    }

}
