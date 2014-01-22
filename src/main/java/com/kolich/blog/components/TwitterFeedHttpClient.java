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

package com.kolich.blog.components;

import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import com.kolich.curacao.handlers.components.ComponentDestroyable;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.concurrent.Future;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.copyLarge;

@Component
public class TwitterFeedHttpClient implements ComponentDestroyable {

    private static final String twitterFeedUrl__ =
        ApplicationConfig.getTwitterFeedUrl();

    private final AsyncHttpClient asyncHttpClient_;

    public TwitterFeedHttpClient() {
        asyncHttpClient_ = new AsyncHttpClient();
    }

    public final Future<TwitterFeed> getTweets() throws IOException {
        return asyncHttpClient_.prepareGet(twitterFeedUrl__)
            .execute(new AsyncCompletionHandler<TwitterFeed>() {
                @Override
                public TwitterFeed onCompleted(final Response r) throws Exception {
                    return new TwitterFeed(r.getResponseBody(UTF_8.toString()));
                }
            });
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        asyncHttpClient_.close();
    }

    public static final class TwitterFeed extends AppendableCuracaoEntity {

        private static final String JSON_UTF_8_TYPE = JSON_UTF_8.toString();

        private final String jsonString_;

        public TwitterFeed(final String jsonString) {
            jsonString_ = jsonString;
        }

        @Override
        public final void toWriter(final Writer writer) throws Exception {
            try(final Reader reader = new StringReader(jsonString_)) {
                copyLarge(reader, writer);
            }
        }

        @Override
        public final int getStatus() {
            return SC_OK;
        }

        @Override
        public final String getContentType() {
            return JSON_UTF_8_TYPE;
        }

    }

}
