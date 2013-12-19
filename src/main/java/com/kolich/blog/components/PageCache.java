package com.kolich.blog.components;

import com.kolich.blog.components.util.MarkdownCacheComponent;
import com.kolich.blog.entities.Page;
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
    public final MarkdownEntityBuilder<Page> getBuilder() {
        return new MarkdownEntityBuilder<Page>() {
            @Override
            public Page build(final String name,
                              final File markdown,
                              final String hash,
                              final Long timestamp,
                              final DiffEntry.ChangeType changeType) {
                return new Page(name, markdown, hash, timestamp, changeType);
            }
        };
    }

    @Override
    public final String getContentDirectory() {
        return PAGE_CONTENT_DIR_NAME;
    }

    @Nullable
    public final Page getPage(final String name) {
        return get(name);
    }

}
