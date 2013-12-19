package com.kolich.blog.components.cache;

import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import org.eclipse.jgit.diff.DiffEntry;

import javax.annotation.Nullable;
import java.io.File;

@Component
public final class EntryCache extends MarkdownCacheComponent<Entry> {

    private static final String ENTRIES_CONTENT_DIR_NAME = "entries";

    @Injectable
    public EntryCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Entry getEntity(final String name,
                                 final File markdown,
                                 final String hash,
                                 final Long timestamp,
                                 final DiffEntry.ChangeType changeType) {
        return new Entry(name, markdown, hash, timestamp, changeType);
    }

    @Override
    public final String getContentDirectory() {
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

}
