package com.kolich.blog.entities;

import java.io.File;
import java.util.Date;

public abstract class MarkdownContentWithHeaderAndFooter
    extends MarkdownContent {

    private final transient File header_;
    private final transient File footer_;

    public MarkdownContentWithHeaderAndFooter(final ContentType type,
                                              final String name,
                                              final String title,
                                              final String hash,
                                              final Date date,
                                              final File content,
                                              final File header,
                                              final File footer) {
        super(type, name, title, hash, date, content);
        header_ = header;
        footer_ = footer;
    }

    public final File getHeader() {
        return header_;
    }

    public final File getFooter() {
        return footer_;
    }

}
