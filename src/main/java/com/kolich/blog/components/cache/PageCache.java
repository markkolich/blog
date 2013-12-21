package com.kolich.blog.components.cache;

import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Page;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import org.eclipse.jgit.diff.DiffEntry;

import javax.annotation.Nullable;
import java.io.File;

@Component
public final class PageCache extends MarkdownCacheComponent<Page> {

    private static final String PAGE_CONTENT_DIR_NAME = "pages";

    @Injectable
    public PageCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Page getEntity(final String name,
                                final String title,
                                final File markdown,
                                final String hash,
                                final Long timestamp,
                                final DiffEntry.ChangeType changeType) {
        return new Page(name, title, markdown, hash, timestamp, changeType);
    }

    @Override
    public final String getContentDirectory() {
        return PAGE_CONTENT_DIR_NAME;
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
