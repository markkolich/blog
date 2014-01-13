package com.kolich.blog.components.cache;

import com.gitblit.models.PathModel;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.gitblit.utils.JGitUtils.getFilesInCommit;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class MarkdownCacheComponent<T extends MarkdownContent>
    implements CuracaoComponent, GitRepository.PullListener {

    private static final Logger logger__ =
        getLogger(MarkdownCacheComponent.class);

    private static final String contentRootDir__ =
        ApplicationConfig.getContentRootDir();

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
            contentRootDir__, getCachedContentDirName()).toString();
        for(final RevCommit commit : git.log().call()) {
            final List<PathModel.PathChangeModel> files =
                getFilesInCommit(repo, commit);
            for(final PathModel.PathChangeModel change : files) {
                final String hash = commit.getName(), // Commit SHA-1 hash
                    name = change.name, // Name of file that was "changed"
                    title = commit.getShortMessage(); // Commit message
                // Commit timestamp, in seconds. Note the conversion to
                // milliseconds because JGit gives us the commit time in
                // seconds... sigh.
                final Date date = new Date(commit.getCommitTime() * 1000L);
                // Change type.
                final DiffEntry.ChangeType type = change.changeType;
                if(name.startsWith(pathToContent) &&
                   type.equals(DiffEntry.ChangeType.ADD)) {
                    final File markdown = new File(repo.getWorkTree(), name);
                    // Only bother adding the markdown content to the map if
                    // the file exists on disk.  Git history may show that
                    // content was added with a given commit, but may have been
                    // since deleted.
                    if(markdown.exists()) {
                        final String entityName = removeExtension(
                            markdown.getName());
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
            if(cache_.size() != newCache.size()) {
                logger__.info("Replacing cache with refreshed content (old=" +
                    cache_.size() + " -> new=" + newCache.size() + "): " +
                    newCache.toString());
            }
            cache_.clear();
            cache_.putAll(newCache);
        }
    }

    protected final T get(final String key) {
        synchronized(cache_) {
            return cache_.get(key);
        }
    }

    protected final int getCount() {
        synchronized(cache_) {
            return cache_.size();
        }
    }

    protected final ImmutableList<T> getAll() {
        synchronized(cache_) {
            return ImmutableList.copyOf(cache_.values());
        }
    }

    protected final PagedContent<T> getAll(@Nullable final Integer limit) {
        final ImmutableList<T> list = getAll();
        final PagedContent<T> result;
        if(limit != null && limit > 0 && limit <= list.size()) {
            final ImmutableList<T> sublist = list.subList(0, limit);
            result = new PagedContent<>(sublist, list.size()-sublist.size());
        } else {
            result = new PagedContent<>(list, 0);
        }
        return result;
    }

    /**
     * Returns all cached content that was committed to the repo before
     * (older, prior to) the given commit.
     */
    protected final PagedContent<T> getAllBefore(@Nullable final String commit,
                                                 @Nullable final Integer limit) {
        // Get an immutable list of all "values" in the current cache map.
        final ImmutableList<T> entries = getAll();
        // If the provided commit hash is null, then we return entries up to
        // a set limit with a remaining zero, indicating none are remaining.
        if(commit == null) {
            return getAll(limit);
        }
        // Find the index of the content corresponding to the provided commit.
        final int index = Iterables.indexOf(entries,
            new Predicate<T>() {
            @Override
            public boolean apply(@Nullable final T input) {
                return commit.equals(input.getCommit());
            }
        });
        if(index < 0) {
            // An entry with the provided commit wasn't found in the set.  This
            // must mean the hash is unknown, so return all entries in addition
            // to the size of the list (indicating all remaining).
            return new PagedContent<>(ImmutableList.<T>of(), entries.size());
        } else {
            final int offset = index + 1;
            final Integer endIndex =
                (limit != null && limit > 0 && offset+limit <= entries.size()) ?
                    offset+limit : entries.size();
            return new PagedContent<>(
                // Sub list, enforcing limit.
                entries.subList(offset, endIndex),
                // The number of entries that remain.
                entries.size() - endIndex);
        }
    }

    public abstract T getEntity(final String name,
                                final String title,
                                final String commit,
                                final Date date,
                                final File content);

    public abstract String getCachedContentDirName();

}
