/**
 * Copyright (c) 2014 Mark S. Kolich
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

package com.kolich.blog.components.cache;

import com.gitblit.models.PathModel;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.*;

import static com.gitblit.utils.JGitUtils.getFilesInCommit;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType.ADD;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractMarkdownCache<T extends MarkdownContent>
    implements CuracaoComponent, GitRepository.PullListener {

    private static final Logger logger__ =
        getLogger(AbstractMarkdownCache.class);

    private static final String contentRootDir__ =
        ApplicationConfig.getContentRootDir();

    private final GitRepository git_;

    /**
     * An internal map that maps the name of each entity to its content.
     */
    private final Map<String,T> cache_;

    /**
     * An internal map that maps each SHA-1 commit hash to a list of
     * content/entries that was written before that commit.  For example if
     * A, B, C, D are a list of commits in order, then list [A, B, C, D] will
     * be translated and cached here as:
     *   A -> [B, C, D]
     *   B -> [C, D]
     *   C -> [D]
     *   D -> []
     *
     * This is so the lookup of "the set of entities that came before a
     * given entity" can be done in constant time O(1).
     */
    private final Map<String,List<T>> shadowCache_;

    public AbstractMarkdownCache(final GitRepository git) {
        git_ = checkNotNull(git, "Git repository object cannot be null.");
        cache_ = Maps.newLinkedHashMap(); // Preserves order
        shadowCache_ = Maps.newLinkedHashMap(); // Preserves order
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
        final List<T> entities = Lists.newLinkedList();
        final Git git = git_.getGit();
        final Repository repo = git_.getRepo();
        // Rebuild the new cache using the "updated" content on disk, if any.
        final String pathToContent = FileSystems.getDefault().getPath(
            contentRootDir__, getCachedContentDirName()).toString();
        for(final RevCommit commit : git.log().call()) {
            final List<PathModel.PathChangeModel> files =
                getFilesInCommit(repo, commit);
            // For each of the files that "changed" (added, removed, modified)
            // in the commit...
            for(final PathModel.PathChangeModel change : files) {
                final String hash = commit.getName(), // Commit SHA-1 hash
                    name = change.name, // Path to file that was "changed"
                    title = commit.getShortMessage(); // Commit message
                // Commit timestamp, in seconds. Note the conversion to
                // milliseconds because JGit gives us the commit time in
                // seconds... sigh.
                final Long timestamp = commit.getCommitTime() * 1000L;
                // Change type.
                final boolean isAdd = change.changeType.equals(ADD);
                if(isAdd && name.startsWith(pathToContent)) {
                    // The "work tree" is where files are checked out
                    // for viewing and editing.
                    final File markdown = new File(repo.getWorkTree(), name);
                    // Only bother adding the markdown content to the map if
                    // the file exists on disk.  Git history may show that
                    // content was added with a given commit, but may have been
                    // since deleted.
                    if(markdown.exists()) {
                        final String entityName = removeExtension(
                            markdown.getName());
                        final T entity = getEntity(entityName, title, hash,
                            timestamp, markdown);
                        // Only add the entity if it's not already in the list.
                        if(!entities.contains(entity)) {
                            entities.add(entity);
                        }
                    }
                }
            }
        }
        // Sort the loaded entities in order based on commit date, not the
        // natural ordering of the commits.  Oldest content/entities fall to
        // the bottom regardless of when they were actually committed.  That
        // is, using the Git environment variables GIT_AUTHOR_DATE and
        // GIT_COMMITTER_DATE, you can commit to a repo at some arbitrary
        // point in the past.
        //  #/> GIT_AUTHOR_DATE='Tue Dec 7 09:32:10 1982 -0800' \
        //      GIT_COMMITTER_DATE='Tue Dec 7 09:32:10 1982 -0800' \
        //      git commit
        // Sorting based on this commit date enforces that older content,
        // recently committed to the repo, are ordered correctly.
        Collections.sort(entities, new Comparator<T>() {
            @Override
            public final int compare(final T a, final T b) {
                return b.getDate().compareTo(a.getDate());
            }
        });
        // Transform the list of entities into a proper map that maps the
        // entity name to itself.  The key of the map is the "name" of the
        // entity, and the value is the entity.
        final Map<String,T> newCache = Maps.uniqueIndex(entities,
            new Function<T,String>() {
                @Nullable @Override
                public String apply(final T input) {
                    checkNotNull(input, "Input cannot be null.");
                    return input.getName();
                }
            });
        // Construct a map which maps each ordered commit hash to the
        // list of content that comes after it.  Note, a SortedSetMultimap
        // could have been used here, but that implementation depends on the
        // natural ordering of the keys and values in the map.  In this case,
        // the ordering isn't the "natural" ordering but is rather dictated by
        // time (e.g., given an entity X, give me all of the stuff older than
        // it in constant time).
        final Map<String,List<T>> newShadowCache = Maps.newLinkedHashMap();
        final Set<Map.Entry<String,T>> sortedEntities = newCache.entrySet();
        for(final Map.Entry<String,T> entity : sortedEntities) {
            boolean includeNext = false;
            final List<T> after = Lists.newLinkedList();
            for(final Map.Entry<String,T> inner : sortedEntities) {
                if(entity.getKey().equals(inner.getKey())) {
                    includeNext = true;
                } else if(includeNext) {
                    after.add(inner.getValue());
                }
            }
            newShadowCache.put(entity.getValue().getCommit(), after);
        }
        // In a thread safe manner, clear the existing cache and then add
        // all new entries into it.  This is essentially just a synchronized
        // "swap" in place.
        synchronized(this) {
            if(cache_.size() != newCache.size()) {
                logger__.info("Replacing cache with refreshed content (old=" +
                    cache_.size() + " -> new=" + newCache.size() + "): " +
                    newCache);
                // Update the entry cache.
                cache_.clear();
                cache_.putAll(newCache);
                // Update the shadow cache.
                shadowCache_.clear();
                shadowCache_.putAll(newShadowCache);
            }
        }
    }

    protected final T get(final String key) {
        synchronized(this) {
            return cache_.get(key);
        }
    }

    protected final ImmutableList<T> getAll() {
        synchronized(this) {
            return ImmutableList.copyOf(cache_.values());
        }
    }

    protected final PagedContent<T> getAll(@Nullable final Integer limit) {
        final List<T> list = getAll();
        final PagedContent<T> result;
        if(limit != null && limit > 0 && limit <= list.size()) {
            final List<T> sublist = list.subList(0, limit);
            result = new PagedContent<>(sublist, list.size() - sublist.size());
        } else {
            result = new PagedContent<>(list, 0);
        }
        return result;
    }

    /**
     * Returns all cached content that was committed to the repo before
     * (older, prior to) the given commit, not including the commit itself.
     */
    protected final PagedContent<T> getAllBefore(@Nullable final String commit,
                                                 @Nullable final Integer limit) {
        final PagedContent<T> result;
        synchronized(this) {
            final List<T> before = ImmutableList.copyOf(shadowCache_.get(commit));
            if(before == null) {
                result = new PagedContent<>(ImmutableList.<T>of(), cache_.size());
            } else {
                final int endIndex =
                    (limit != null && limit > 0 && limit <= before.size()) ?
                        limit : before.size();
                final List<T> sublist = before.subList(0, endIndex);
                result = new PagedContent<>(sublist, before.size() - sublist.size());
            }
        }
        return result;
    }

    public abstract T getEntity(final String name,
                                final String title,
                                final String commit,
                                final Long timestamp,
                                final File content);

    public abstract String getCachedContentDirName();

}
