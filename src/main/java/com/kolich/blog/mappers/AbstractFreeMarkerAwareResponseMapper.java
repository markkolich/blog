package com.kolich.blog.mappers;

import com.google.common.collect.Maps;
import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.FreeMarkerConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.util.Map;

public abstract class AbstractFreeMarkerAwareResponseMapper<T>
    extends AbstractDevModeSafeResponseMapper<T> {

    protected static final String appContextPath__ =
        ApplicationConfig.getContextPath();
    protected static final String blogTitle__ =
        ApplicationConfig.getBlogTitle();
    protected static final String getBlogSubTitle__ =
        ApplicationConfig.getBlogSubTitle();
    protected static final String hostname__  =
        ApplicationConfig.getHostname();
    protected static final String fullUri__ =
        ApplicationConfig.getFullUri();

    protected static final String TEMPLATE_ATTR_CONTEXT_PATH = "context";

    protected static final String TEMPLATE_ATTR_BLOG_TITLE = "blogTitle";
    protected static final String TEMPLATE_ATTR_BLOG_SUBTITLE = "blogSubTitle";
    protected static final String TEMPLATE_ATTR_HOSTNAME = "hostname";
    protected static final String TEMPLATE_ATTR_FULL_URI = "fullUri";

    protected static final String TEMPLATE_ATTR_NAME = "name";
    protected static final String TEMPLATE_ATTR_TITLE = "title";
    protected static final String TEMPLATE_ATTR_COMMIT = "commit";
    protected static final String TEMPLATE_ATTR_DATE = "date";
    protected static final String TEMPLATE_ATTR_CONTENT = "content";

    protected static final String TEMPLATE_ATTR_ENTRIES = "entries";
    protected static final String TEMPLATE_ATTR_ENTRIES_REMAINING = "remaining";

    protected final Configuration config_;

    public AbstractFreeMarkerAwareResponseMapper(final FreeMarkerConfig config) {
        config_ = config.getConfig();
    }

    protected Map<String,Object> getDataMap(final Template tp) {
        final Map<String,Object> map = Maps.newLinkedHashMap();
        map.put(TEMPLATE_ATTR_CONTEXT_PATH, appContextPath__);
        map.put(TEMPLATE_ATTR_BLOG_TITLE, blogTitle__);
        map.put(TEMPLATE_ATTR_BLOG_SUBTITLE, getBlogSubTitle__);
        map.put(TEMPLATE_ATTR_HOSTNAME, hostname__);
        map.put(TEMPLATE_ATTR_FULL_URI, fullUri__);
        return map;
    }

}
