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

package com.kolich.blog.components.cache;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.components.cache.bus.BlogEventBus;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.protos.Events;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.Required;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class EntryCache {

    private static final Logger logger__ = getLogger(EntryCache.class);

    private static final String entriesDir__ = ApplicationConfig.getEntriesDir();

    /**
     * The blog internal state machine; passes events between components.
     */
    private final BlogEventBus eventBus_;

    /**
     * An internal map that maps the name of each entity to its content, maintaining insertion order.
     */
    private final Map<String, Entry> cache_;

    /**
     * A set of unsorted entries as read from the Git repo on cache build; is a temporary holding object
     * which is read from later to build out the cache map.
     */
    private final Set<Entry> unsortedEntries_;

    /**
     * The full canonical path to the directory on disk that holds the entry markdown files.
     */
    private final String canonicalEntriesDir_;

    @Injectable
    public EntryCache(@Required final GitRepository repo,
                      @Required final BlogEventBus eventBus) {
        cache_ = Maps.newLinkedHashMap(); // Preserves insertion order, important
        unsortedEntries_ = Sets.newLinkedHashSet();
        canonicalEntriesDir_ = repo.getFileRelativeToContentRoot(entriesDir__).getAbsolutePath();
        eventBus_ = eventBus;
        eventBus_.register(this);
    }

    @Subscribe
    public synchronized final void onStartReadCachedContent(final Events.StartReadCachedContentEvent e) {
        logger__.debug("onStartReadCachedContent: {}", e);
        unsortedEntries_.clear();
    }

    @Subscribe
    public synchronized final void onCachedContent(final Events.CachedContentEvent e) {
        logger__.trace("onCachedContent: START: {}", e);
        // Only bother with the event if the incoming event refers to content in a location in the repo
        // this cache is concerned about.
        if (!e.getFile().startsWith(canonicalEntriesDir_)) {
            return;
        }
        // Build out a new entry entity and fork based on the event's operation.
        final Entry entry = new Entry(e.getName(), e.getTitle(), e.getMsg(), e.getHash(), e.getTimestamp(), e.getFile());
        final Events.CachedContentEvent.Operation op = e.getOperation();
        if (Events.CachedContentEvent.Operation.ADD.equals(op)) {
            final File markdownFile = entry.getMarkdownFile().getFile();
            // If the markdown file actually exists on disk (hasn't been deleted) and the cache
            // doesn't already contain a value for this entry then add it.  This prevents an entry from
            // being added to the cache, deleted, and then re-added again with the wrong commit message/title.
            if (markdownFile.exists() && !unsortedEntries_.contains(entry)) {
                unsortedEntries_.add(entry);
            } else if (!markdownFile.exists()) {
                unsortedEntries_.remove(entry);
            }
        } else {
            logger__.trace("Received unsupported/unknown event: {}", e);
        }
    }

    @Subscribe
    public synchronized final void onEndReadCachedContent(final Events.EndReadCachedContentEvent e) {
        logger__.trace("onEndReadCachedContent: START: {}", e);
        // Sort the loaded entities in order based on commit date, not the natural ordering of the commits.
        // Oldest content/entities fall to the bottom regardless of when they were actually committed.  That
        // is, using the Git environment variables GIT_AUTHOR_DATE and GIT_COMMITTER_DATE, you can commit to a
        // repo at some arbitrary point in the past.
        //  #/> GIT_AUTHOR_DATE='Tue Dec 7 09:32:10 1982 -0800' \
        //      GIT_COMMITTER_DATE='Tue Dec 7 09:32:10 1982 -0800' \
        //      git commit
        // Sorting based on this commit date enforces that older content, recently committed to the repo, are
        // ordered correctly.
        final List<Entry> sorted = unsortedEntries_.stream().sorted((a, b) -> b.getDate().compareTo(a.getDate()))
            .collect(Collectors.toList());
        // Transform the list of entities into a proper map that maps the entity name to itself.  The key of the
        // map is the "name" of the entity, and the value is the entity.
        final Map<String, Entry> newCache = Maps.uniqueIndex(sorted, new Function<Entry, String>() {
            @Nullable @Override
            public String apply(final Entry input) {
                checkNotNull(input, "Input cannot be null.");
                return input.getName();
            }
        });
        // Clear the existing cache and then add all new entries into it.  This is essentially just a
        // synchronized "swap" in place.
        cache_.clear();
        cache_.putAll(newCache);
        // Probably not really needed, but clear out the unsorted entries set for good measure.
        unsortedEntries_.clear();
        logger__.debug("onEndReadCachedContent: END: {} -> {}", e, cache_);
        // Let any listeners know that the "entry cache" is ready and willing.
        eventBus_.post(Events.EntryCacheReadyEvent.newBuilder()
            .setUuid(UUID.randomUUID().toString())
            .setTimestamp(System.currentTimeMillis())
            .build());
    }

    public synchronized final Entry get(final String key) {
        final Entry e = cache_.get(key);
        if (e == null) {
            throw new ContentNotFoundException("Failed to load entry for key: " + key);
        }
        return e;
    }

    /**
     * Returns an immutable list of all content in this cache, in sorted order.
     */
    public synchronized final List<Entry> getAll() {
        return ImmutableList.copyOf(cache_.values());
    }

    public synchronized final PagedContent<Entry> getAll(@Nullable final Integer limit) {
        final List<Entry> list = getAll();
        final PagedContent<Entry> result;
        if(limit != null && limit > 0 && limit <= list.size()) {
            final List<Entry> sublist = list.subList(0, limit);
            result = new PagedContent<>(sublist, list.size() - sublist.size());
        } else {
            result = new PagedContent<>(list, 0);
        }
        return result;
    }

    public synchronized final AtomRss getAtomFeed(final int limit) {
        return new AtomRss(getAll(limit));
    }

    public synchronized final Sitemap getSitemap() {
        return new Sitemap(getAll());
    }

}
