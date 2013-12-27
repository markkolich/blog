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
public final class PageCache extends MarkdownCacheComponent<Page> {

    private static final String pagesDir__ =
        ApplicationConfig.getPagesDir();

    @Injectable
    public PageCache(final GitRepository git) {
        super(git);
    }

    @Override
    public final Page getEntity(final String name,
                                final String title,
                                final String hash,
                                final Date date,
                                final File content) {
        return new Page(name, title, hash, date, content);
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
