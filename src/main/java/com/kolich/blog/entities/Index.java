package com.kolich.blog.entities;

import java.util.List;

public final class Index extends MarkdownContent {

    private static final String TEMPLATE_NAME = "index.ftl";

    private final List<Entry> entries_;

    public Index(final List<Entry> entries) {
        super(ContentType.INDEX, null, null, null, null, null);
        entries_ = entries;
    }

    public final List<Entry> getEntries() {
        return entries_;
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
