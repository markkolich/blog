package com.kolich.blog.entities;

import com.kolich.blog.entities.gson.PagedContent;

import java.util.List;

public final class Index extends MarkdownContent {

    private static final String TEMPLATE_NAME = "index.ftl";

    private final PagedContent<Entry> entries_;

    public Index(final PagedContent<Entry> entries) {
        super(ContentType.INDEX, null, null, null, null);
        entries_ = entries;
    }

    public final List<Entry> getEntries() {
        return entries_.getContent();
    }

    public final int getRemaining() {
        return entries_.getRemaining();
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
