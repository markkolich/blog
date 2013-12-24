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
    implements CuracaoComponent, GitRepository.PullListener {

    private static final Logger logger__ =
        getLogger(MarkdownCacheComponent.class);

    private static final String markdownRootDir__ =
        ApplicationConfig.getMarkdownRootDir();

    private final GitRepository git_;
    private final Map<String,T> cache_;

    public MarkdownCacheComponent(final GitRepository git) {
        git_ = checkNotNull(git, "Git repository object cannot be null.");
        cache_ = Maps.newLinkedHashMap();
    }

    @Override
    public final void initialize(final ServletContext context)
        throws Exception {
        git_.registerListener(this);
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        git_.unRegisterListener(this);
    }

    @Override
    public final void onPull() throws Exception {
        final Map<String,T> newCache = Maps.newLinkedHashMap();
        final Git git = git_.getGit();
        final Repository repo = git_.getRepo();
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

    public abstract T getEntity(final String name,
                                final String title,
                                final String hash,
                                final Date date,
                                final File content);

    public abstract String getContentDirectoryName();

}
