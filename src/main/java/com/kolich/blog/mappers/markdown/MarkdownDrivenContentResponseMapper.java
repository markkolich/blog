/**
 * Copyright (c) 2014 Mark S. Kolich
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

package com.kolich.blog.mappers.markdown;

import com.kolich.blog.components.FreeMarkerConfig;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.html.Utf8TextEntity;
import com.kolich.blog.mappers.AbstractFreeMarkerAwareResponseMapper;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import freemarker.template.Template;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.kolich.blog.entities.html.Utf8TextEntity.TextEntityType.HTML;

@ControllerReturnTypeMapper(MarkdownContent.class)
public final class MarkdownDrivenContentResponseMapper
    extends AbstractFreeMarkerAwareResponseMapper<MarkdownContent> {

    @Injectable
    public MarkdownDrivenContentResponseMapper(final FreeMarkerConfig config) {
        super(config);
    }

    @Override
    public final Utf8TextEntity renderView(@Nonnull final MarkdownContent md)
        throws Exception {
        final Template tp = getTemplate(md.getTemplateName());
        return buildEntity(tp, getDataMap(tp, md), HTML);
    }

    protected Map<String,Object> getDataMap(final Template tp,
                                            final MarkdownContent md) {
        final Map<String,Object> map = super.getDataMap(tp);
        final Object name = tp.getCustomAttribute(TEMPLATE_ATTR_NAME);
        map.put(TEMPLATE_ATTR_NAME, (name == null) ? md.getName() : name);
        final Object title = tp.getCustomAttribute(TEMPLATE_ATTR_TITLE);
        map.put(TEMPLATE_ATTR_TITLE, (title == null) ? md.getTitle() : title);
        final Object hash = tp.getCustomAttribute(TEMPLATE_ATTR_COMMIT);
        map.put(TEMPLATE_ATTR_COMMIT, (hash == null) ? md.getCommit() : hash);
        final Object date = tp.getCustomAttribute(TEMPLATE_ATTR_DATE);
        map.put(TEMPLATE_ATTR_DATE, (date == null) ? md.getDateFormatted() : date);
        // Attach the Markdown content converted to a String to the data map.
        final String content;
        if((content = md.getContent()) != null) {
            map.put(TEMPLATE_ATTR_CONTENT, content);
        }
        // Only Index types get a list of entries and a remaining count
        // attached to their data map.
        if(md instanceof Index) {
            final Index idx = (Index)md;
            map.put(TEMPLATE_ATTR_ENTRIES, idx.getEntries());
            map.put(TEMPLATE_ATTR_ENTRIES_REMAINING, idx.getRemaining());
        }
        return map;
    }

}
