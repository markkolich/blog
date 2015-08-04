/**
 * Copyright (c) 2015 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.blog.components;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.cache.bus.BlogEventBus;
import com.kolich.blog.protos.Events;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.Required;
import com.kolich.curacao.components.CuracaoComponent;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class GitRepository implements CuracaoComponent {

    private static final Logger logger__ = getLogger(GitRepository.class);

    private static final Boolean isDevMode__ = ApplicationConfig.isDevMode();
    private static final String blogRepoCloneUrl__ = ApplicationConfig.getBlogRepoCloneUrl();
    private static final String clonePath__ = ApplicationConfig.getClonePath();
    private static final Boolean shouldCloneOnStartup__ = ApplicationConfig.shouldCloneOnStartup();
    private static final String contentRootDir__ = ApplicationConfig.getContentRootDir();
    private static final Long gitUpdateInterval__ = ApplicationConfig.getGitPullUpdateInterval();

    private static final String userDir__ = System.getProperty("user.dir");

    private final Repository repo_;
    private final Git git_;

    private final BlogEventBus eventBus_;
    private final ScheduledExecutorService executor_;

    @Injectable
    public GitRepository(@Required final ServletContext context,
                         @Required final BlogEventBus eventBus) throws Exception {
        eventBus_ = eventBus;
        executor_ = newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("blog-git-puller")
            .build());
        final File repoDir = getRepoDir(context);
        logger__.info("Activated repository path: {}", repoDir.getCanonicalFile());
        // If were not in dev mode, and the clone path doesn't exist or we need to force clone from
        // scratch, do that now.
        final boolean clone = (!repoDir.exists() || shouldCloneOnStartup__);
        if (!isDevMode__ && clone) {
            logger__.info("Clone path does not exist, or we were asked to re-clone. Cloning from: {}",
                blogRepoCloneUrl__);
            // Recursively delete the existing clone of the repo, if it exists, and clone the repository.
            FileUtils.deleteDirectory(repoDir);
            Git.cloneRepository()
                .setURI(blogRepoCloneUrl__)
                .setDirectory(repoDir)
                .setBranch("release")
                .call();
        }
        // Construct a new pointer to the repository on disk.
        repo_ = new FileRepositoryBuilder()
            .setWorkTree(repoDir)
            .readEnvironment()
            .build();
        git_ = new Git(repo_);
        logger__.info("Successfully initialized repository at: {}", repo_);
    }

    @Override
    public final void initialize() throws Exception {
        // Schedule a new updater at a "fixed" interval that has no
        // initial delay to fetch/pull in new content immediately.
        executor_.scheduleAtFixedRate(
            new GitPuller(), // new puller
            0L, // initial delay, start ~now~
            gitUpdateInterval__, // repeat every
            TimeUnit.MILLISECONDS); // units
    }

    @Override
    public final void destroy() throws Exception {
        // Close our handle to the repository on shutdown.
        repo_.close();
        // Ask the single thread updater pool to "shutdown".
        if (executor_ != null) {
            executor_.shutdown();
        }
    }

    private static final File getRepoDir(final ServletContext context) {
        final File repoDir;
        if (isDevMode__) {
            // If in "development mode", the clone path is the local copy of the repo on disk.
            repoDir = new File(userDir__);
        } else {
            // Otherwise, it could be somewhere else either in an absolute location (/...) or somewhere
            // under the Servlet container when relative.
            repoDir = (clonePath__.startsWith(File.separator)) ?
                // If the specified clone path in the application configuration is "absolute", then use that path raw.
                new File(clonePath__) :
                // Otherwise, the clone path is relative to the container Servlet context of this application.
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

    private class GitPuller implements Runnable {

        /**
         * The number of times a Git pull has to fail before it shows up in the logs.  Often there are intermittent
         * failures with GitHub where the network is down or the GitHub infrastructure is having problems; in those
         * cases, we don't want to bother logging the failure on every pull because it will very likely succeed if we
         * try again a few moments later.
         */
        private static final int COMPLAINT_THRESHOLD = 12; // 12 = 60 mins / 5

        private int failures_ = 0;

        @Override
        public final void run() {
            try {
                // Only attempt a "pull" if we're not in development mode. We should not pull when in dev mode,
                // because of pending changes that are likely waiting to be committed may unexpectedly interfere
                // with the pull (merge conflicts, etc.)
                if (!isDevMode__) {
                    git_.pull().call();
                }
                // Async post a message to the event bus indicating that a pull event has fired *and*
                // completed successfully.
                final Events.GitPullEvent gitPullEvent = Events.GitPullEvent.newBuilder()
                    .setUuid(UUID.randomUUID().toString())
                    .setTimestamp(System.currentTimeMillis())
                    .build();
                eventBus_.post(gitPullEvent);
                failures_ = 0; // Reset counter, pull completed successfully.
            } catch (Exception e) {
                // Only log the failure if we've failed enough times to warrant some log spew.
                if (++failures_ >= COMPLAINT_THRESHOLD) {
                    logger__.warn("Failed to Git 'pull', raw Git operation did not complete successfully.", e);
                }
            }
        }

    }

}
