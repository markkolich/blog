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

package com.kolich.blog.entities.feed;

import com.google.common.collect.Iterables;
import com.kolich.blog.entities.Entry;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractFeedEntity {

    private final List<Entry> entries_;

    public AbstractFeedEntity(final List<Entry> entries) {
        entries_ = checkNotNull(entries, "Entries list cannot be null.");
    }

    public final List<Entry> getEntries() {
        return entries_;
    }

    /**
     * Returns the first entry in the list of entries, or null if there was no first entry.
     */
    public final Entry getFirst() {
        return Iterables.getFirst(entries_, null);
    }

}
