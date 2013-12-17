package com.kolich.blog.entities;

import java.io.File;

public final class Page extends MarkdownDrivenContent {

    public static final String PAGE_CONTENT_DIR_NAME = "pages";

    public Page(final File markdown) {
        super(markdown);
    }

}
