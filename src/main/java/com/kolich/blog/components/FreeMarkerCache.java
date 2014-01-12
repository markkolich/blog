package com.kolich.blog.components;

import com.google.common.base.Charsets;
import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;

@Component
public final class FreeMarkerCache {

    private static final String templatesDir__ =
        ApplicationConfig.getTemplatesDir();

    private final File templateRoot_;

    @Injectable
    public FreeMarkerCache(final GitRepository git) {
        templateRoot_ = git.getFileRelativeToContentRoot(templatesDir__);
    }

    public final Configuration getConfig() throws IOException {
        final Configuration config = new Configuration();
        config.setDirectoryForTemplateLoading(templateRoot_);
        config.setDefaultEncoding(Charsets.UTF_8.name());
        return config;
    }

}
