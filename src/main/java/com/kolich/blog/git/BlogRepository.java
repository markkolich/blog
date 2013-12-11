package com.kolich.blog.git;

import com.kolich.blog.BlogConfigurationFactory;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class BlogRepository implements CuracaoComponent {

    private static final Logger logger__ = getLogger(BlogRepository.class);

    private static final String blogRepoCloneUrl__ =
        BlogConfigurationFactory.getBlogRepoCloneUrl();
    private static final String clonePath__ =
        BlogConfigurationFactory.getClonePath();
    private static final Boolean shouldCloneFromScratchOnStartup__ =
        BlogConfigurationFactory.shouldCloneFromScratchOnStartup();

    private Repository repository_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        final File realClonePath = (clonePath__.startsWith("/")) ?
            // If the specified clone path in the application configuration
            // is "absolute", then use that path raw.
            new File(clonePath__) :
            // Otherwise, the clone path is relative to the container Servlet
            // context of this application.
            new File(context.getRealPath("/" + clonePath__));
        logger__.info("Using repository clone path: " +
            realClonePath.getCanonicalFile());
        // If the desired clone path doesn't exist yet, or we were asked to
        // clone fresh on application startup, then go for it.
        if(!realClonePath.exists() || shouldCloneFromScratchOnStartup__) {
            logger__.info("Clone path does not exist, or we were asked " +
                "to re-clone fresh on startup. So, cloning...");
            logger__.info("Using Git repo URL: " + blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo,
            // if it exists.
            FileUtils.deleteDirectory(realClonePath);
            // Clone the repository to disk.
            Git.cloneRepository()
                .setURI(blogRepoCloneUrl__)
                .setDirectory(realClonePath)
                .setBranch("master")
                .call();
        }
        // Construct a new pointer to the repository on disk.
        repository_ = new FileRepositoryBuilder()
            .setWorkTree(realClonePath)
            .readEnvironment()
            .build();
        // Pull a fresh copy of the repo from the remote.  Yes, we may have
        // just cloned it fresh, but this is what we do, bandwidth is cheap.
        new Git(repository_).pull().call();
        logger__.info("Successfully initialized Git repository: " +
            repository_);
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        repository_.close();
    }

    public Repository getContentRepository() {
        return repository_;
    }

}
