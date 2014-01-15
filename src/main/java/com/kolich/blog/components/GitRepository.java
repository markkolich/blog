package com.kolich.blog.components;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
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
    private static final String contentRootDir__ =
        ApplicationConfig.getContentRootDir();
    private static final Long gitUpdateInterval__ =
        ApplicationConfig.getGitPullUpdateInterval();

    private static final String userDir__ = System.getProperty("user.dir");

    private final Repository repo_;
    private final Git git_;

    private final ScheduledExecutorService executor_;

    /**
     * A set of {@link PullListener}'s which are notified in order when this
     * Git repository is updated on disk (e.g., when a "pull" request
     * finishes).
     */
    private final Set<PullListener> listeners_;

    @Injectable
    public GitRepository(final ServletContext context) throws Exception {
        executor_ = newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("git-puller")
                .build());
        listeners_ = Sets.newLinkedHashSet();
        final File repoDir = getRepoDir(context);
        logger__.info("Activated repository path: " +
            repoDir.getCanonicalFile());
        // If were not in dev mode, and the clone path doesn't exist or we need
        // to force clone from scratch, do that now.
        final boolean clone = (!repoDir.exists() || shouldCloneOnStartup__);
        if(!isDevMode__ && clone) {
            logger__.info("Clone path does not exist, or we were asked " +
                "to re-clone. Cloning from: " + blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo, if it exists,
            // and clone the repository.
            FileUtils.deleteDirectory(repoDir);
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
        logger__.info("Successfully initialized repository at: " + repo_);
    }

    @Override
    public final void initialize(final ServletContext context) throws Exception {
        // Schedule a new updater at a "fixed" interval that has no
        // initial delay to fetch/pull in new content immediately.
        executor_.scheduleAtFixedRate(
            new GitPuller(), // new puller
            0L,  // initial delay, start ~now~
            gitUpdateInterval__, // repeat every
            TimeUnit.MILLISECONDS); // units
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        // Close our handle to the repository on shutdown.
        repo_.close();
        // Ask the single thread updater pool to "shutdown".
        if(executor_ != null) {
            executor_.shutdown();
        }
    }

    private static final File getRepoDir(final ServletContext context) {
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
        return repoDir;
    }

    @Nonnull
    public final Git getGit() {
        return git_;
    }

    @Nonnull
    public final Repository getRepo() {
        return repo_;
    }

    @Nonnull
    public final File getContentRoot() {
        return new File(repo_.getWorkTree(), contentRootDir__);
    }

    @Nonnull
    public final File getFileRelativeToContentRoot(final String child) {
        return new File(getContentRoot(), child);
    }

    public final boolean registerListener(final PullListener listener) {
        return listeners_.add(listener);
    }
    public final boolean unRegisterListener(final PullListener listener) {
        return listeners_.remove(listener);
    }

    public interface PullListener {
        public void onPull() throws Exception;
    }

    private class GitPuller implements Runnable {

        @Override
        public final void run() {
            try {
                // Only attempt a "pull" if we're not in development mode.
                // We should not pull when in dev mode, because of pending
                // changes that are likely waiting to be committed may
                // unexpectedly interfere with the pull (merge conflicts, etc.)
                if(!isDevMode__) {
                    git_.pull().call();
                }
                // Notify each pull listener that the pull is done and they can
                // update their caches as needed.
                for(final PullListener listener : listeners_) {
                    try {
                        listener.onPull();
                    } catch (Exception e) {
                        logger__.warn("Failure notifying pull listener.", e);
                    }
                }
            } catch (Exception e) {
                logger__.warn("Failed to Git 'pull', raw Git operation " +
                    "did not complete successfully.", e);
            }
        }

    }

}
