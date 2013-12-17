package com.kolich.blog.entities;

import javax.annotation.Nonnull;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MarkdownDrivenContent {

    private final File markdown_;

    public MarkdownDrivenContent(@Nonnull final File markdown) {
        markdown_ = checkNotNull(markdown, "Markdown file cannot be null.");
    }

    public final File getMarkdown() {
        return markdown_;
    }

}
