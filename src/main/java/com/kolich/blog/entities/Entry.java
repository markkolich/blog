package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Entry extends MarkdownContent {

    public Entry(final String name,
                 final String title,
                 final String hash,
                 final Date date,
                 final File content) {
        super(ContentType.ENTRY, name, title, hash, date, content);
    }

}
