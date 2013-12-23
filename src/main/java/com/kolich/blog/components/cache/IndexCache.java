package com.kolich.blog.components.cache;

import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.Page;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

@Component
public final class IndexCache extends MarkdownCacheComponent<Index> {

    private static final String PAGE_CONTENT_DIR_NAME = "pages";

    @Injectable
    public IndexCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Index getEntity(final String name,
                                 final String title,
                                 final String hash,
                                 final Date date,
                                 final File content) {
        return new Index(name, title, hash, date, content);
    }

    @Override
    public final String getContentDirectoryName() {
        return PAGE_CONTENT_DIR_NAME;
    }

    @Nullable
    public final Index getIndex(final String name) {
        final Index i;
        if((i = get(name)) == null) {
            throw new ContentNotFoundException("Failed to load index for " +
                "key: " + name);
        }
        return i;
    }

}
