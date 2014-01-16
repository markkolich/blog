package com.kolich.blog.mappers.feed;

import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.feed.AbstractFeedEntity;
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
        final Entry first;
        if((first = entity.getFirst()) != null) {
            map.put(TEMPLATE_ATTR_LAST_UPDATED,
                entity.getDateFormatted(first));
        }
        map.put(TEMPLATE_ATTR_ENTRIES, entity.getEntries());
        return map;
    }

}
