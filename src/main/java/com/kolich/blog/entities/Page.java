package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Page extends MarkdownContent {

    private static final String TEMPLATE_NAME = "page.ftl";

    public Page(final String name,
                final String title,
                final String commit,
                final Date date,
                final File content) {
        super(ContentType.PAGE, name, title, commit, date, content);
    }

    @Override
    public final String getTemplateName() {
        return TEMPLATE_NAME;
    }

}
