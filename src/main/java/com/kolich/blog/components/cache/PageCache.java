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

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.GitRepository;
import com.kolich.blog.components.cache.bus.BlogEventBus;
import com.kolich.blog.entities.Page;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.blog.protos.Events;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.Required;
import org.slf4j.Logger;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public final class PageCache {

    private static final Logger logger__ = getLogger(PageCache.class);

    private static final String pagesDir__ = ApplicationConfig.getPagesDir();

    /**
     * The blog internal state machine; passes events between components.
     */
    private final BlogEventBus eventBus_;

    /**
     * An internal map that maps the name of each entity to its content.
     */
    private final Map<String, Page> cache_;

    /**
     * The full canonical path to the directory on disk that holds the page markdown files.
     */
    private final String canonicalPagesDir_;

    @Injectable
    public PageCache(@Required final GitRepository repo,
                     @Required final BlogEventBus eventBus) {
        eventBus_ = eventBus;
        eventBus_.register(this);
        cache_ = Maps.newLinkedHashMap();
        canonicalPagesDir_ = repo.getFileRelativeToContentRoot(pagesDir__).getAbsolutePath();
    }

    @Subscribe
    public synchronized final void onCachedContent(final Events.CachedContentEvent e) {
        logger__.trace("onCachedContent: START: {}", e);
        // Only bother with the event if the incoming event refers to content in a location in the
        // repo this cache is concerned about.
        if (!e.getFile().startsWith(canonicalPagesDir_)) {
            return;
        }
        // Build out a new page entity and fork based on the event's operation.
        final Page page = new Page(e.getName(), e.getTitle(), e.getMsg(), e.getHash(), e.getTimestamp(), e.getFile());
        final Events.CachedContentEvent.Operation op = e.getOperation();
        if (Events.CachedContentEvent.Operation.ADD.equals(op)) {
            cache_.put(page.getName(), page);
        } else if (Events.CachedContentEvent.Operation.DELETE.equals(op)) {
            cache_.remove(page.getName());
        } else {
            logger__.trace("Received unsupported/unknown event: {}", e);
        }
    }

    public synchronized final Page getPage(final String name) {
        final Page p = cache_.get(name);
        if (p == null) {
            throw new ContentNotFoundException("Failed to load page for key: " + name);
        }
        return p;
    }

}
