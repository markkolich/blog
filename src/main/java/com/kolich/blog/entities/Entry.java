package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Entry extends MarkdownContent {

    private static final String TEMPLATE_NAME = "entry.ftl";

    public Entry(final String name,
                 final String title,
                 final String commit,
                 final Date date,
                 final File content) {
        super(ContentType.ENTRY, name, title, commit, date, content);
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
