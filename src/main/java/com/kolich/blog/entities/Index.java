package com.kolich.blog.entities;

import java.util.Set;

public final class Index extends MarkdownContent {

    private static final String TEMPLATE_NAME = "index.ftl";

    private final Set<Entry> entries_;

    public Index(final Set<Entry> entries) {
        super(ContentType.INDEX, null, null, null, null, null);
        entries_ = entries;
    }

    public final Set<Entry> getEntries() {
        return entries_;
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
