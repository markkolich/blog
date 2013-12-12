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

    private static final Boolean isDevMode__ =
        BlogConfigurationFactory.isDevMode();
    private static final String blogRepoCloneUrl__ =
        BlogConfigurationFactory.getBlogRepoCloneUrl();
    private static final String clonePath__ =
        BlogConfigurationFactory.getClonePath();
    private static final Boolean shouldCloneFromScratchOnStartup__ =
        BlogConfigurationFactory.shouldCloneFromScratchOnStartup();

    private static final String userDir__ = System.getProperty("user.dir");

    private File clonePath_;
    private Repository repository_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        if(isDevMode__) {
            // If in "development mode", the clone path is the local copy of
            // the repo on disk.
            clonePath_ = new File(userDir__);
        } else {
            // Otherwise, it could be somewhere else either in an absolute
            // location (/...) or somewhere under the Servlet container when
            // relative.
            clonePath_ = (clonePath__.startsWith(File.separator)) ?
                // If the specified clone path in the application configuration
                // is "absolute", then use that path raw.
                new File(clonePath__) :
                // Otherwise, the clone path is relative to the container Servlet
                // context of this application.
                new File(context.getRealPath(File.separator + clonePath__));
        }
        logger__.info("Using repository clone path: " +
            clonePath_.getCanonicalFile());
        // If were not in dev mode, and the clone path doesn't exist or we need
        // to force clone from scratch, do that now...
        final boolean clone = (!clonePath_.exists() || shouldCloneFromScratchOnStartup__);
        if(!isDevMode__ && clone) {
            logger__.info("Clone path does not exist, or we were asked " +
                "to re-clone fresh on startup. So, cloning...");
            logger__.info("Using Git repo URL: " + blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo,
            // if it exists.
            FileUtils.deleteDirectory(clonePath_);
            // Clone the repository to disk.
            Git.cloneRepository()
                .setURI(blogRepoCloneUrl__)
                .setDirectory(clonePath_)
                .setBranch("master")
                .call();
        }
        // Construct a new pointer to the repository on disk.
        repository_ = new FileRepositoryBuilder()
            .setWorkTree(clonePath_)
            .readEnvironment()
            .build();
        if(!isDevMode__) {
            // Pull a fresh copy of the repo from the remote.  Yes, we may have
            // just cloned it fresh, but this is what we do, bandwidth is cheap.
            new Git(repository_).pull().call();
        }
        logger__.info("Successfully initialized Git repository: " +
            repository_);
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        repository_.close();
    }

    public File getClonePath() {
        return clonePath_;
    }

    public Repository getContentRepository() {
        return repository_;
    }

}
