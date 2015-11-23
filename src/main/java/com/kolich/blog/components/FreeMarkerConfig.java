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

package com.kolich.blog.components;

import com.google.common.base.Charsets;
import com.kolich.blog.ApplicationConfig;
import curacao.annotations.Component;
import curacao.annotations.Injectable;
import curacao.annotations.Required;
import freemarker.template.Configuration;

import java.io.File;
import java.io.IOException;

@Component
public final class FreeMarkerConfig {

    private static final String templatesDir__ = ApplicationConfig.getTemplatesDir();

    private static final String UTF_8_CHARSET = Charsets.UTF_8.name();

    private final Configuration config_;

    @Injectable
    public FreeMarkerConfig(@Required final GitRepository git) throws IOException {
        final File templateRoot = git.getFileRelativeToContentRoot(templatesDir__);
        config_ = new Configuration(Configuration.VERSION_2_3_22);
        config_.setDirectoryForTemplateLoading(templateRoot);
        config_.setDefaultEncoding(UTF_8_CHARSET);
    }

    public final Configuration getConfig() {
        return config_;
    }

}
