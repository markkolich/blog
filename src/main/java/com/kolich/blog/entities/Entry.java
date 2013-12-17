package com.kolich.blog.entities;

import org.eclipse.jgit.diff.DiffEntry;

import javax.annotation.Nullable;
import java.io.File;

public final class Entry extends MarkdownDrivenContent {

    public static final String ENTRIES_CONTENT_DIR_NAME = "entries";

    private final String hash_;
    private final DiffEntry.ChangeType changeType_;

    public Entry(final File markdown, @Nullable final String hash,
        @Nullable final DiffEntry.ChangeType changeType) {
        super(markdown);
        hash_ = hash;
        changeType_ = changeType;
    }

    public final String getHash() {
        return hash_;
    }

    public final DiffEntry.ChangeType getChangeType() {
        return changeType_;
    }

}
