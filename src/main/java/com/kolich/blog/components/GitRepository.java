package com.kolich.blog.components;

import com.kolich.blog.ApplicationConfig;
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
public final class GitRepository implements CuracaoComponent {

    private static final Logger logger__ = getLogger(GitRepository.class);

    private static final Boolean isDevMode__ =
        ApplicationConfig.isDevMode();
    private static final String blogRepoCloneUrl__ =
        ApplicationConfig.getBlogRepoCloneUrl();
    private static final String clonePath__ =
        ApplicationConfig.getClonePath();
    private static final Boolean shouldCloneOnStartup__ =
        ApplicationConfig.shouldCloneOnStartup();

    private static final String userDir__ = System.getProperty("user.dir");

    private Repository repo_;
    private Git git_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        final File repoDir;
        if(isDevMode__) {
            // If in "development mode", the clone path is the local copy of
            // the repo on disk.
            repoDir = new File(userDir__);
        } else {
            // Otherwise, it could be somewhere else either in an absolute
            // location (/...) or somewhere under the Servlet container when
            // relative.
            repoDir = (clonePath__.startsWith(File.separator)) ?
                // If the specified clone path in the application configuration
                // is "absolute", then use that path raw.
                new File(clonePath__) :
                // Otherwise, the clone path is relative to the container Servlet
                // context of this application.
                new File(context.getRealPath(File.separator + clonePath__));
        }
        logger__.info("Using repository clone path: " +
            repoDir.getCanonicalFile());
        // If were not in dev mode, and the clone path doesn't exist or we need
        // to force clone from scratch, do that now...
        final boolean clone = (!repoDir.exists() || shouldCloneOnStartup__);
        if(!isDevMode__ && clone) {
            logger__.info("Clone path does not exist, or we were asked " +
                "to re-clone. So, cloning from: " + blogRepoCloneUrl__);
            logger__.info("Using Git repo URL: " + blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo,
            // if it exists.
            FileUtils.deleteDirectory(repoDir);
            // Clone the repository to disk.
            Git.cloneRepository()
                .setURI(blogRepoCloneUrl__)
                .setDirectory(repoDir)
                .setBranch("master")
                .call();
        }
        // Construct a new pointer to the repository on disk.
        repo_ = new FileRepositoryBuilder()
            .setWorkTree(repoDir)
            .readEnvironment()
            .build();
        git_ = new Git(repo_);
        if(!isDevMode__) {
            // Pull a fresh copy of the repo from the remote.  Yes, we may have
            // just cloned it fresh, but this is what we do, bandwidth is cheap.
            git_.pull().call();
        }
        logger__.info("Successfully initialized Git repository: " + repo_);
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        repo_.close();
    }

    public final Git getGit() {
        return git_;
    }

    public final Repository getRepo() {
        return repo_;
    }

}
