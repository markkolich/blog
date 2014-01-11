package com.kolich.blog.components;

import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.exceptions.DirectoryListingException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;

import java.io.File;
import java.nio.file.Paths;

@Component
public final class StaticFileResolver {

    private final GitRepository git_;

    @Injectable
    public StaticFileResolver(final GitRepository git) {
        git_ = git;
    }

    public final File getStaticFileInContentRoot(final String uri) {
        final File file = new File(git_.getContentRoot(),
            Paths.get(uri).toString());
        if(!file.exists()) {
            throw new ContentNotFoundException("Could not find static " +
                "resource file: " + file.getAbsolutePath());
        } else if(file.isDirectory()) {
            throw new DirectoryListingException("Will not list contents " +
                "of 'directory': " + file.getAbsolutePath());
        }
        return file;
    }

}
