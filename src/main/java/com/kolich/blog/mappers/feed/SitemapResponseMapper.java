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
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.entities.html.Utf8TextEntity;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.Mapper;
import com.kolich.curacao.annotations.Required;
import freemarker.template.Template;

import javax.annotation.Nonnull;

import static com.kolich.blog.entities.html.Utf8TextEntity.TextEntityType.XML;

@Mapper
public final class SitemapResponseMapper extends AbstractFeedEntityResponseMapper<Sitemap> {

    private static final String SITEMAP_TEMPLATE_NAME = "feed/sitemap.ftl";

    @Injectable
    public SitemapResponseMapper(@Required final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final Utf8TextEntity renderView(@Nonnull final Sitemap sm) throws Exception {
        final Template tp = getTemplate(SITEMAP_TEMPLATE_NAME);
        return buildEntity(tp, getDataMap(tp, sm), XML);
    }

}
