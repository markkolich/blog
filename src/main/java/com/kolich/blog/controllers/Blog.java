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

    @GET("^\\/$")
    public final Index index() {
        return new Index(entries_.getEntries(entryLimit__));
    }
    @GET("^\\/about$")
    public final Page about() {
        return pages_.getPage(PAGE_ABOUT);
    }
    @GET("^\\/contact$")
    public final Page contact() {
        return pages_.getPage(PAGE_CONTACT);
    }

    // Blog posts/entries

    @GET("^\\/(?<name>[a-zA-Z_0-9\\-]+)$")
    public final Entry entry(@Path("name") final String name) {
        return entries_.getEntry(name);
    }

    // Static resources

    @GET("^(?<resource>\\/static\\/[a-zA-Z_0-9\\-\\.\\/]+)$")
    public final File staticFile(@Path("resource") final String resource) {
        return staticResolver_.getStaticFileInContentRoot(resource);
    }

    // Templatized content

    @GET("^\\/atom\\.xml$")
    public final AtomRss atomFeed() {
        return entries_.getAtomFeed(entryLimitAtomFeed__);
    }
    @GET("^\\/sitemap\\.xml$")
    public final Sitemap sitemap() {
        return entries_.getSitemap();
    }
    @GET("^\\/robots\\.txt$")
     public final RobotsTxt robots() {
        return new RobotsTxt();
    }
    @GET("^\\/humans\\.txt$")
    public final HumansTxt humans() {
        return new HumansTxt();
    }

    // APIs

    @GET("^\\/blog\\.json$")
    public final PagedContent<Entry> jsonFeed(@Query("before") final String commit) {
        return entries_.getEntriesBefore(commit, entryLimitLoadMore__);
    }
    @GET("^\\/tweets\\.json$")
    public final Future<TwitterFeed> tweets() throws IOException {
        return twitterClient_.getTweets();
    }

}
