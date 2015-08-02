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

package com.kolich.blog.mappers.feed;

import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.feed.AbstractFeedEntity;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.mappers.AbstractFreeMarkerAwareResponseMapper;
import freemarker.template.Template;

import java.util.Map;

public abstract class AbstractFeedEntityResponseMapper<T extends AbstractFeedEntity>
    extends AbstractFreeMarkerAwareResponseMapper<T> {

    public AbstractFeedEntityResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    protected Map<String,Object> getDataMap(final Template tp,
                                            final T entity) {
        final Map<String,Object> map = super.getDataMap(tp);
        // Get the "last updated" date and timestamp from the first entry in the entry collection.  This is used
        // only for Atom/RSS feeds and XML sitemaps.
        final Entry first;
        if((first = entity.getFirst()) != null) {
            String result = null;
            // If the incoming entity is of type RSS feed, then we extract the Atom/RSS specific date from the
            // entity.  Otherwise, use the default sitemap date which is much less granular.
            if(entity instanceof AtomRss) {
                result = first.getAtomFeedDateFormatted();
            } else if(entity instanceof Sitemap) {
                result = first.getSitemapDateFormatted();
            }
            map.put(TEMPLATE_ATTR_LAST_UPDATED, result);
        }
        map.put(TEMPLATE_ATTR_ENTRIES, entity.getEntries());
        return map;
    }

}
