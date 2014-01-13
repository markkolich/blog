package com.kolich.blog.entities;

import com.kolich.blog.entities.gson.PagedContent;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FeedContent {

    private final List<Entry> entries_;

    public FeedContent(final List<Entry> entries) {
        entries_ = checkNotNull(entries, "Entries list cannot be null.");
    }

    public FeedContent(final PagedContent<Entry> entries) {
        this(entries.getContent());
    }

    public final List<Entry> getEntries() {
        return entries_;
    }

}
