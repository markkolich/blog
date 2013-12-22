package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Entry extends MarkdownContentWithHeaderAndFooter {

    public Entry(final String name,
                 final String title,
                 final String hash,
                 final Date date,
                 final File content,
                 final File header,
                 final File footer) {
        super(ContentType.ENTRY, name, title, hash, date, content,
            header, footer);
    }

}
