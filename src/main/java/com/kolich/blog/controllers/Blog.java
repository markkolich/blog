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

package com.kolich.blog.controllers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.StaticFileResolver;
import com.kolich.blog.components.TwitterFeedHttpClient;
import com.kolich.blog.components.TwitterFeedHttpClient.TwitterFeed;
import com.kolich.blog.components.cache.EntryCache;
import com.kolich.blog.components.cache.PageCache;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.Page;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.entities.txt.HumansTxt;
import com.kolich.blog.entities.txt.RobotsTxt;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.parameters.Path;
import com.kolich.curacao.annotations.parameters.Query;
import com.kolich.curacao.annotations.parameters.RequestUri;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

@Controller
public final class Blog {

    private static final int entryLimit__ =
        ApplicationConfig.getHomepageEntryLimit();
    private static final int entryLimitLoadMore__ =
        ApplicationConfig.getLoadMoreEntryLimit();
    private static final int entryLimitAtomFeed__ =
        ApplicationConfig.getAtomFeedEntryLimit();

    private static final String PAGE_ABOUT = "about";
    private static final String PAGE_CONTACT = "contact";

    private final EntryCache entries_;
    private final PageCache pages_;

    private final TwitterFeedHttpClient twitterClient_;
    private final StaticFileResolver staticResolver_;

    @Injectable
    public Blog(final EntryCache entries,
                final PageCache pages,
                final TwitterFeedHttpClient twitterClient,
                final StaticFileResolver staticResolver) {
        entries_ = entries;
        pages_ = pages;
        twitterClient_ = twitterClient;
        staticResolver_ = staticResolver;
    }

    // Pages

    @GET("/")
    public final Index index() {
        return new Index(entries_.getEntries(entryLimit__));
    }
    @GET("/about")
    public final Page about() {
        return pages_.getPage(PAGE_ABOUT);
    }
    @GET("/contact")
    public final Page contact() {
        return pages_.getPage(PAGE_CONTACT);
    }

    // Static content

    @GET("/static/**")
    public final File staticFile(@RequestUri(includeContext=false) final String uri) {
        return staticResolver_.getStaticFileInContentRoot(uri);
    }

    // Templatized content

    @GET("/atom.xml")
    public final AtomRss atomFeed() {
        return entries_.getAtomFeed(entryLimitAtomFeed__);
    }
    @GET("/sitemap.xml")
    public final Sitemap sitemap() {
        return entries_.getSitemap();
    }
    @GET("/robots.txt")
     public final RobotsTxt robots() {
        return new RobotsTxt();
    }
    @GET("/humans.txt")
    public final HumansTxt humans() {
        return new HumansTxt();
    }

    // APIs

    @GET("/blog.json")
    public final PagedContent<Entry> jsonFeed(@Query("before") final String commit) {
        return entries_.getEntriesBefore(commit, entryLimitLoadMore__);
    }
    @GET("/tweets.json")
    public final Future<TwitterFeed> tweets() throws IOException {
        return twitterClient_.getTweets();
    }

    // Blog posts/entries

    @GET("/{name}/**")
    public final Entry entry(@Path("name") final String name,
                             @RequestUri(includeContext=false) final String uri) {
        // This is somewhat of a hack fix to reject requests for entries
        // with extra "junk" on the end of the URI.  For example, this request
        // succeeds, but shouldn't:
        // GET:/howto-setting-up-your-own-local-dns-server/RK=0/RS=CCrz8_YjIovuMb5b2hbLGY_8AOo-
        // Note the "/RK=0/RS=CCrz8_YjIovuMb5b2hbLGY_8AOo-" which should trigger
        // the application to return a 404 Not Found instead of serving up a
        // 200 OK with actual entry content for post "howto-setting-up-your-own-local-dns-server".
        // This is due to the Ant style path matcher which is the only supported
        // path matching mechanism in the Curacao stack today.  If Curacao supported
        // regexp path matching, this could be a non-issue with the regex.  Filed
        // an issue to track missing regexp support in Curacao:
        // https://github.com/markkolich/curacao/issues/1
        if(!uri.endsWith(name)) {
            throw new ContentNotFoundException("Request URI contained extra " +
                "junk, not just entry name (name=" + name + ", uri=" +
                uri + ")");
        }
        return entries_.getEntry(name);
    }

}
