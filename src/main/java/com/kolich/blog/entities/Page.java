package com.kolich.blog.entities;

import org.eclipse.jgit.diff.DiffEntry;

import java.io.File;

public final class Page extends MarkdownDrivenContent {

    public Page(final String name,
                final String title,
                final File markdown,
                final String hash,
                final Long timestamp,
                final DiffEntry.ChangeType changeType) {
        super(name, title, markdown, hash, timestamp, changeType);
    }

}
