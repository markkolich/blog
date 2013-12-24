package com.kolich.blog.components;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.Component;
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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final String markdownRootDir__ =
        ApplicationConfig.getMarkdownRootDir();
    private static final Long gitUpdateInterval__ =
        ApplicationConfig.getGitPullUpdateInterval();

    private static final String userDir__ = System.getProperty("user.dir");

    private Repository repo_;
    private Git git_;

    private final ScheduledExecutorService executor_;
    private final Set<PullListener> listeners_;

    public GitRepository() {
        executor_ = newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("git-pull'er")
                .build());
        listeners_ = Sets.newLinkedHashSet();
    }

    @Override
    public void initialize(final ServletContext context) throws Exception {
        final File repoDir = getRepoDir(context);
        logger__.info("Active repository path: " + repoDir.getCanonicalFile());
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
        logger__.info("Successfully initialized Git repository: " + repo_);
        // Schedule a new updater at a "fixed" interval that has no
        // initial delay to fetch/pull in new content immediately.
        executor_.scheduleAtFixedRate(
            new GitPuller(this), // new puller
            0L,  // initial delay, start ~now~
            gitUpdateInterval__, // repeat every
            TimeUnit.MILLISECONDS); // units
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        repo_.close();
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
    public final File getMarkdownRoot() {
        return new File(repo_.getWorkTree(), markdownRootDir__);
    }

    @Nonnull
    public final File getFileRelativeToMarkdownRoot(final String child) {
        return new File(getMarkdownRoot(), child);
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

        private final AtomicBoolean lock_;

        public GitPuller(final GitRepository gitRepo) {
            lock_ = new AtomicBoolean(false);
        }

        @Override
        public final void run() {
            if(lock_.compareAndSet(false, true)) {
                try {
                    // Only attempt a "pull" if we're not in development mode.
                    // We should not pull when in dev mode, because of pending
                    // changes that are likely waiting to be committed may
                    // unexpectedly interfere with the pull command (merge
                    // conflicts, etc.).
                    if(!isDevMode__) {
                        git_.pull().call();
                    }
                    for(final PullListener listener : listeners_) {
                        try {
                            listener.onPull();
                        } catch (Exception e) {
                            logger__.warn("Pull listener failed to update.", e);
                        }
                    }
                } catch (Exception e) {
                    logger__.warn("Failed to Git 'pull'", e);
                } finally {
                    lock_.set(false);
                }
            }
        }

    }

}
