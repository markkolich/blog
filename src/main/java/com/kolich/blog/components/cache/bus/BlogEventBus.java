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

package com.kolich.blog.components.cache.bus;

import com.google.common.eventbus.EventBus;
import curacao.annotations.Component;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event bus allows publish-subscribe-style communication between components without requiring
 * the components to explicitly register with one another (and thus be aware of each other).  It is
 * designed exclusively to replace traditional Java in-process event distribution using explicit
 * registration.
 *
 * Used here as an internal state machine to pass messages and events between components.
 */
@Component
public final class BlogEventBus {

    private final EventBus bus_;

    public BlogEventBus() {
        bus_ = new EventBus(BlogEventBus.class.getCanonicalName());
    }

    public final void register(@Nonnull final Object o) {
        checkNotNull(o, "Registering object cannot be null.");
        bus_.register(o);
    }

    public final void unregister(@Nonnull final Object o) {
        checkNotNull(o, "Un-registering object cannot be null.");
        bus_.unregister(o);
    }

    public final void post(@Nonnull final Object message) {
        checkNotNull(message, "Message to post to bus cannot be null.");
        bus_.post(message);
    }

}
