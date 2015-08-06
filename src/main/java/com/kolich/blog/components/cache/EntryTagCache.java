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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.eventbus.Subscribe;
import com.kolich.blog.components.cache.bus.BlogEventBus;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.EntryTag;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.protos.Events;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.Required;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This in-memory cache maps a list of tags to a set of {@link Entry} entities that are tagged
 * with those keywords.  This is used for finding other content similar to a given {@link Entry}
 * as tagged by the entry author.
 */
@Component
public final class EntryTagCache {

    private static final Logger logger__ = getLogger(EntryTagCache.class);

    /**
     * A private reference to the internal entry cache.
     */
    private final EntryCache entryCache_;

    /**
     * An internal multimap which maps a tag/keyword to a list of content
     * that are referenced by that tag.
     */
    private final Multimap<String, Entry> tagCache_;

    @Injectable
    public EntryTagCache(@Required final EntryCache entryCache,
                         @Required final BlogEventBus eventBus) {
        entryCache_ = entryCache;
        tagCache_ = LinkedHashMultimap.create(); // Preserves order
        eventBus.register(this);
    }

    @Subscribe
    public synchronized final void onEntryCacheReady(final Events.EntryCacheReadyEvent e) {
        logger__.trace("onEntryCacheReady: START: {}", e);
        tagCache_.clear();
        // Get all entries, and iterate over them.
        final List<Entry> all = entryCache_.getAll();
        for (final Entry entry : all) {
            // Get all tags, if any, on the entry.
            final List<EntryTag> tags = entry.getTags();
            // Note we're indexing the entries based on their "URL encoded tag" text. This is so that we
            // don't have to URL decode the incoming tag from the request URI on lookup.  In other words,
            // if the URI is /tagged/foo+bar then the key in this map is literally the URL encoded "foo+bar",
            // and not "foo bar".
            tags.stream().forEach(tag -> tagCache_.put(tag.getUrlEncodedText(), entry));
        }
        logger__.debug("onEntryCacheReady: END: {} -> {}", e, tagCache_);
    }

    public synchronized final PagedContent<Entry> getAllTagged(@Nonnull final String tag) {
        checkNotNull(tag, "Content tag cannot be null.");
        final Collection<Entry> tagged = tagCache_.get(tag);
        return new PagedContent<>(ImmutableList.copyOf(tagged), 0);
    }

}
