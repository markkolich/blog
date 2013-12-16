package com.kolich.blog.entities;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MarkdownDrivenContent {

    private final String name_;

    public MarkdownDrivenContent(final String name) {
        name_ = checkNotNull(name, "Content name cannot be null.");
    }

    public final String getName() {
        return name_;
    }

}
