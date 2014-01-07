package com.kolich.blog.components;

import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.exceptions.DirectoryListingException;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.handlers.components.CuracaoComponent;

import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.Paths;

@Component
public final class StaticFileResolver implements CuracaoComponent {

    private final GitRepository git_;

    @Injectable
    public StaticFileResolver(final GitRepository git) {
        git_ = git;
    }

    public final File getStaticFileInContentRoot(final String uri) {
        final File f = new File(git_.getContentRoot(), Paths.get(uri).toString());
        if(!f.exists()) {
            throw new ContentNotFoundException("Could not find static " +
                "resource file: " + f.getAbsolutePath());
        } else if(f.isDirectory()) {
            throw new DirectoryListingException("Will not list contents " +
                "of 'directory': " + f.getAbsolutePath());
        }
        return f;
    }

    @Override
    public final void initialize(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

}
