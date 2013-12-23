package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Index extends MarkdownContent {

    private static final String TEMPLATE_NAME = "index.ftl";

    public Index(final String name,
                 final String title,
                 final String hash,
                 final Date date,
                 final File content) {
        super(ContentType.INDEX, name, title, hash, date, content);
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
