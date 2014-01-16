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
        // Get the "last updated" date and timestamp from the first entry
        // in the entry collection.  This is used only for Atom/RSS feeds and
        // XML sitemaps.
        final Entry first;
        if((first = entity.getFirst()) != null) {
            String result = null;
            // If the incoming entity is of type RSS feed, then we extract the
            // Atom/RSS specific date from the entity.  Otherwise, use the
            // default sitemap date which is much less granular.
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
