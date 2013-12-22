package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Page extends MarkdownContent {

    public Page(final String name,
                final String title,
                final String hash,
                final Date date,
                final File content) {
        super(ContentType.PAGE, name, title, hash, date, content);
    }

}
