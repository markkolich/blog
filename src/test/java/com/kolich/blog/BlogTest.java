/**
 * Copyright (c) 2016 Mark S. Kolich
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

package com.kolich.blog;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kolich.blog.components.cache.bus.BlogEventBus;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.gson.GsonAppendableBlogEntity;
import com.kolich.blog.protos.Events;
import curacao.test.CuracaoJUnit4Runner;
import curacao.test.annotations.CuracaoJUnit4RunnerConfig;
import curacao.test.annotations.MockComponent;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;

@RunWith(CuracaoJUnit4Runner.class)
@CuracaoJUnit4RunnerConfig(contextPath = "/blog")
public final class BlogTest {

    private static final Logger log = LoggerFactory.getLogger(BlogTest.class);

    private static final String UNIT_TEST_URL = "http://localhost:8080/blog/";

    private static final class EntryTuple implements Comparable<EntryTuple> {
        public final String commit_;
        public final Date commitDate_;
        public EntryTuple(final String commit, final Date commitDate) {
            commit_ = checkNotNull(commit, "Commit hash cannot be null.");
            commitDate_ = checkNotNull(commitDate, "Commit date cannot be null.");
        }
        @Override
        public final int compareTo(final EntryTuple o) {
            return commitDate_.compareTo(o.commitDate_);
        }
        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryTuple that = (EntryTuple) o;
            return Objects.equal(commit_, that.commit_);
        }
        @Override
        public final int hashCode() {
            return Objects.hashCode(commit_);
        }
    }

    /**
     * Use our own "mock" event bus because we need to inject this unit test into
     * the mix to listen for bus events, like "entry cache ready" which is our trigger
     * to safely start testing the web-application container.
     */
    @MockComponent
    private final BlogEventBus eventBus_;

    /**
     * The async HTTP client that we use to make real requests on-the-wire.
     */
    private final AsyncHttpClient httpClient_;

    /**
     * The latch is used internally to wait for the entry cache to be ready for we
     * can proceed; when the web-application container starts, it asynchronously reads
     * entries from commits and loads them into the entry cache.  The latch hits zero
     * when all commits are read meaning we're released and allowed to start testing.
     */
    private final CountDownLatch latch_;

    public BlogTest() {
        eventBus_ = new BlogEventBus();
        eventBus_.register(this);

        httpClient_ = new DefaultAsyncHttpClient();
        latch_ = new CountDownLatch(1);
    }

    @Subscribe
    public void onCacheReady(final Events.EntryCacheReadyEvent e) {
        latch_.countDown();
    }
    @Before
    public void before() throws Exception {
        latch_.await();
    }

    @Test
    public void testIndex() throws Exception {
        final Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL).execute();
        final Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/html;charset=utf-8", r.getContentType());

        final String responseBody = r.getResponseBody(Charsets.UTF_8);
        final Document d = Jsoup.parse(responseBody);

        final Elements title = d.select("title");
        assertEquals(1, title.size());
        final String titleText = title.get(0).text();
        assertTrue(titleText.startsWith(ApplicationConfig.getBlogTitle()));

        final Elements entries = d.select("div.entry");
        assertEquals(ApplicationConfig.getHomepageEntryLimit(), entries.size());
    }

    @Test
    public void testAbout() throws Exception {
        final Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "about").execute();
        final Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/html;charset=utf-8", r.getContentType());

        final String responseBody = r.getResponseBody(Charsets.UTF_8);

        assertNotNull(responseBody);
        assertTrue(responseBody.contains("About Mark"));
    }

    @Test
    public void testAtomRss() throws Exception {
        Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "atom.xml").execute();
        Response r = f.get();
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/xml;charset=utf-8", r.getContentType());

        final String responseBody = r.getResponseBody(Charsets.UTF_8);
        final Document d = Jsoup.parse(responseBody);

        final Elements entries = d.select("entry");
        assertEquals(ApplicationConfig.getAtomFeedEntryLimit(), entries.size());

        // Validate that each URL in the atom/rss feed is reachable (will resolve correctly)
        for (final Element entry : entries) {
            final Elements links = entry.select("link");
            assertEquals(1, links.size());

            final Element link = links.get(0);
            assertTrue(link.hasAttr("rel"));
            assertTrue(link.hasAttr("type"));
            assertTrue(link.hasAttr("href"));

            String href = link.attr("href");
            assertTrue(href.startsWith("http://"));

            f = httpClient_.prepareGet(href).execute();
            r = f.get(2, TimeUnit.SECONDS);

            assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
            assertEquals("text/html;charset=utf-8", r.getContentType());

            log.debug("Loaded {} successfully!", href);
        }
    }

    @Test
    public void testSitemapXml() throws Exception {
        Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "sitemap.xml").execute();
        Response r = f.get();
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/xml;charset=utf-8", r.getContentType());

        final String responseBody = r.getResponseBody(Charsets.UTF_8);
        final Document d = Jsoup.parse(responseBody);

        final Elements urlset = d.select("urlset");
        assertEquals(1, urlset.size());

        final Elements urls = urlset.select("url");
        assertTrue(urls.size() > 0);

        // Validate that each URL in the sitemap XML is reachable (will resolve correctly)
        for (final Element url : urls) {
            assertTrue(url.children().size() >= 2);

            final Element loc = url.child(0);
            final String href = loc.text();

            f = httpClient_.prepareGet(href).execute();
            r = f.get(2, TimeUnit.SECONDS);

            assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
            assertEquals("text/html;charset=utf-8", r.getContentType());

            log.debug("Loaded {} successfully!", href);
        }
    }

    @Test
    public void testBeforeJsonApi() throws Exception {
        final Gson gson = GsonAppendableBlogEntity.getNewBlogGsonInstance();

        String url = UNIT_TEST_URL + "api/blog.json";
        Future<Response> f = httpClient_.prepareGet(url).execute();
        Response r = f.get();

        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("application/json; charset=utf-8", r.getContentType());

        String responseJson = r.getResponseBody(Charsets.UTF_8);
        JsonObject obj = gson.fromJson(responseJson, JsonObject.class);

        assertTrue(obj.has("first"));

        int shouldHaveCount = obj.get("remaining").getAsInt();

        String entryHash = obj.get("first").getAsString();

        final List<EntryTuple> visitedEntries = Lists.newArrayList();
        final List<EntryTuple> visitedEntriesSorted = Lists.newArrayList();

        int visited = 1;
        for (; entryHash != null; visited++) {
            url = UNIT_TEST_URL + "api/blog.json?before=" + entryHash;
            f = httpClient_.prepareGet(url).execute();
            r = f.get();

            assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
            assertEquals("application/json; charset=utf-8", r.getContentType());

            responseJson = r.getResponseBody(Charsets.UTF_8);
            obj = gson.fromJson(responseJson, JsonObject.class);

            assertTrue(obj.has("content"));
            assertTrue(obj.has("first"));
            assertTrue(obj.has("remaining"));

            // Done
            if (obj.get("remaining").getAsInt() == 0) {
                break;
            }

            final JsonArray contentArray = obj.getAsJsonArray("content");
            assertEquals(1, contentArray.size());

            final JsonObject content = contentArray.get(0).getAsJsonObject();
            assertTrue(content.has("type"));
            assertTrue(content.has("name"));
            assertTrue(content.has("message"));
            assertTrue(content.has("commit"));
            assertTrue(content.has("date"));
            assertTrue(content.has("html"));

            final EntryTuple entry = new EntryTuple(entryHash, MarkdownContent.BlogContentDateFormat.getNewInstance()
                .parse(content.get("date").getAsString()));
            visitedEntries.add(entry);
            visitedEntriesSorted.add(entry);

            entryHash = content.get("commit").getAsString();

            log.debug("Successfully loaded {} \n\t now going to commit {}", url, entryHash);
        }

        assertEquals(shouldHaveCount, visitedEntries.size()+1);
        assertEquals(shouldHaveCount, visited);

        Collections.sort(visitedEntriesSorted, Collections.reverseOrder());
        assertEquals(visitedEntries, visitedEntriesSorted);
    }

    @Test
    public void testRobotsTxt() throws Exception {
        final Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "robots.txt").execute();
        final Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/plain;charset=utf-8", r.getContentType());
    }
    @Test
    public void testHumansTxt() throws Exception {
        final Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "humans.txt").execute();
        final Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/plain;charset=utf-8", r.getContentType());
    }

    @Test
    public void testStaticAssets() throws Exception {
        // Test release JavaScript
        Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "static/release/blog.js").execute();
        Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("application/javascript; charset=utf-8", r.getContentType());

        // Test release CSS
        f = httpClient_.prepareGet(UNIT_TEST_URL + "static/release/blog.css").execute();
        r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_OK, r.getStatusCode());
        assertEquals("text/css; charset=utf-8", r.getContentType());

        // Test asset not found
        f = httpClient_.prepareGet(UNIT_TEST_URL + "static/foo.jpg").execute();
        r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, r.getStatusCode());
        assertEquals("text/html;charset=utf-8", r.getContentType());
    }

    @Test
    public void testNotFound() throws Exception {
        final Future<Response> f = httpClient_.prepareGet(UNIT_TEST_URL + "foo").execute();
        final Response r = f.get(2, TimeUnit.SECONDS);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, r.getStatusCode());
        assertEquals("text/html;charset=utf-8", r.getContentType());

        final String responseBody = r.getResponseBody(Charsets.UTF_8);
        assertTrue(responseBody.contains("404 Not Found"));
    }

}
