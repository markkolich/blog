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

    private final GitRepository git_;

    @Injectable
    public FreeMarkerCache(final GitRepository git) throws IOException {
        git_ = git;
    }

    public final Configuration getConfig(final String dirName) throws IOException {
        final File templateRoot = git_.getFileRelativeToContentRoot(dirName);
        final Configuration config = new Configuration();
        config.setDirectoryForTemplateLoading(templateRoot);
        config.setDefaultEncoding(Charsets.UTF_8.name());
        return config;
    }

    public final Configuration getConfig() throws IOException {
        return getConfig(templatesDir__);
    }

}
