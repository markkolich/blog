package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public final class Page extends MarkdownContentWithHeaderAndFooter {

    public Page(final String name,
                final String title,
                final String hash,
                final Date date,
                final File content,
                final File header,
                final File footer) {
        super(ContentType.PAGE, name, title, hash, date, content,
            header, footer);
    }

}
