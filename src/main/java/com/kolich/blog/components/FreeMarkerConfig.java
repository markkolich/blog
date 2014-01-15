package com.kolich.blog.components;

import com.google.common.base.Charsets;
import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.annotations.Injectable;
import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;

@Component
public final class FreeMarkerConfig {

    private static final String templatesDir__ =
        ApplicationConfig.getTemplatesDir();

    private static final String UTF_8_CHARSET_NAME = Charsets.UTF_8.name();

    private final Configuration config_;

    @Injectable
    public FreeMarkerConfig(final GitRepository git) throws IOException {
        final File templateRoot = git.getFileRelativeToContentRoot(templatesDir__);
        config_ = new Configuration();
        config_.setDirectoryForTemplateLoading(templateRoot);
        config_.setDefaultEncoding(UTF_8_CHARSET_NAME);
    }

    public final Configuration getConfig() {
        return config_;
    }

}
