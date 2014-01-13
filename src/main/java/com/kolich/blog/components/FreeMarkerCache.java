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

    private final Configuration config_;

    @Injectable
    public FreeMarkerCache(final GitRepository git) throws IOException {
        final File templateRoot = git.getFileRelativeToContentRoot(templatesDir__);
        config_ = new Configuration();
        config_.setDirectoryForTemplateLoading(templateRoot);
        config_.setDefaultEncoding(Charsets.UTF_8.name());
    }

    public final Configuration getConfig() {
        return config_;
    }

}
