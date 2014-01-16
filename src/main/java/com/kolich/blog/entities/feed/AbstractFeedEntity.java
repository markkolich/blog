package com.kolich.blog.entities.feed;

import com.google.common.collect.Iterables;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.gson.PagedContent;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractFeedEntity {

    private final List<Entry> entries_;

    public AbstractFeedEntity(final List<Entry> entries) {
        entries_ = checkNotNull(entries, "Entries list cannot be null.");
    }

    public AbstractFeedEntity(final PagedContent<Entry> entries) {
        this(entries.getContent());
    }

    public final List<Entry> getEntries() {
        return entries_;
    }

    public final Entry getFirst() {
        return Iterables.getFirst(entries_, null);
    }

}
