package com.kolich.blog.components.cache;

import com.gitblit.models.PathModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gitblit.utils.JGitUtils.getFilesInCommit;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class MarkdownCacheComponent<T extends MarkdownContent>
    implements CuracaoComponent {

    private static final Logger logger__ =
        getLogger(MarkdownCacheComponent.class);

    private static final Boolean isDevMode__ =
        ApplicationConfig.isDevMode();
    private static final String markdownRootDir__ =
        ApplicationConfig.getMarkdownRootDir();
    private static final Long gitUpdateInterval =
        ApplicationConfig.getGitUpdateInterval();

    private final GitRepository git_;
    private final Map<String,T> cache_;

    private final ScheduledExecutorService executor_;

    public MarkdownCacheComponent(final GitRepository git) {
        git_ = checkNotNull(git, "Git repository object cannot be null.");
        cache_ = Maps.newLinkedHashMap();
        executor_ = newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("blog-git-updater")
                .build());
    }

    @Override
    public final void initialize(final ServletContext context)
        throws Exception {
        // Schedule a new updater at a "fixed" interval that has no
        // initial delay to fetch/pull in new content immediately.
        executor_.scheduleAtFixedRate(
            new CacheUpdater<>(this), // new updater
            0L,  // initial delay, start ~now~
            gitUpdateInterval, // repeat every
            TimeUnit.MILLISECONDS); // units
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        if(executor_ != null) {
            executor_.shutdown();
        }
    }

    protected final T get(final String key) {
        synchronized(cache_) {
            return cache_.get(key);
        }
    }

    protected final Map<String,T> getAll() {
        synchronized(cache_) {
            return ImmutableMap.copyOf(cache_);
        }
    }

    private final void updateCache() throws Exception {
        final Map<String,T> newCache = Maps.newLinkedHashMap();
        final Git git = git_.getGit();
        final Repository repo = git_.getRepo();
        // Only git pull when we're not in development mode.
        if(!isDevMode__) {
            git.pull().call();
        }
        // Rebuild the new cache using the "updated" content, if any.
        final String pathToContent = FileSystems.getDefault().getPath(
            markdownRootDir__, getContentDirectoryName()).toString();
        for(final RevCommit commit : git.log().call()) {
            final List<PathModel.PathChangeModel> files =
                getFilesInCommit(repo, commit);
            for(final PathModel.PathChangeModel change : files) {
                final String hash = change.objectId,
                    name = change.name,
                    title = commit.getShortMessage();
                final Date date = new Date(commit.getCommitTime() * 1000L);
                final DiffEntry.ChangeType type = change.changeType;
                if(name.startsWith(pathToContent) && type.equals(DiffEntry.ChangeType.ADD)) {
                    final File markdown = new File(repo.getWorkTree(), name);
                    // Only bother adding the markdown content to the map if
                    // the file exists on disk.  Git history may show that
                    // content was added with a given commit, but
                    if(markdown.exists()) {
                        final String entityName = removeExtension(markdown.getName());
                        final T entity = getEntity(entityName, title, hash,
                            date, markdown);
                        newCache.put(entityName, entity);
                    }
                }
            }
        }
        // In a thread safe manner, clear the existing cache and then add
        // all new entries into it.  This is essentially just a "swap".
        synchronized(cache_) {
            logger__.debug("Replacing cache with refreshed content " +
                "(old=" + cache_.size() + ", new=" + newCache.size() + ")");
            cache_.clear();
            cache_.putAll(newCache);
        }
    }

    public abstract T getEntity(final String name,
                                final String title,
                                final String hash,
                                final Date date,
                                final File content);

    public abstract String getContentDirectoryName();

    private static class CacheUpdater<S extends MarkdownContent>
        implements Runnable {

        private final AtomicBoolean lock_;
        private final MarkdownCacheComponent<S> component_;

        public CacheUpdater(MarkdownCacheComponent<S> component) {
            lock_ = new AtomicBoolean(false);
            component_ = component;
        }

        @Override
        public final void run() {
            if(lock_.compareAndSet(false, true)) {
                try {
                    logger__.debug("Starting cache update...");
                    component_.updateCache();
                } catch (Exception e) {
                    logger__.warn("Failed to Git pull update " +
                        "markdown cache.", e);
                } finally {
                    lock_.set(false);
                }
            }
        }

    }

}
