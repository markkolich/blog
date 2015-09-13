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

package com.kolich.blog.controllers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.StaticFileResolver;
import com.kolich.blog.components.TwitterFeedHttpClient;
import com.kolich.blog.components.TwitterFeedHttpClient.TwitterFeed;
import com.kolich.blog.components.cache.EntryCache;
import com.kolich.blog.components.cache.EntryShadowCache;
import com.kolich.blog.components.cache.EntryTagCache;
import com.kolich.blog.components.cache.PageCache;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.Page;
import com.kolich.blog.entities.Tagged;
import com.kolich.blog.entities.feed.AtomRss;
import com.kolich.blog.entities.feed.Sitemap;
import com.kolich.blog.entities.gson.PagedContent;
import com.kolich.blog.entities.txt.HumansTxt;
import com.kolich.blog.entities.txt.RobotsTxt;
import com.kolich.blog.exceptions.ContentNotFoundException;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.RequestMapping;
import com.kolich.curacao.annotations.Required;
import com.kolich.curacao.annotations.parameters.Path;
import com.kolich.curacao.annotations.parameters.Query;

import java.io.File;

@Controller
public final class Blog {

    private static final int entryLimitHomepage__ = ApplicationConfig.getHomepageEntryLimit();
    private static final int entryLimitLoadMore__ = ApplicationConfig.getLoadMoreEntryLimit();
    private static final int entryLimitAtomFeed__ = ApplicationConfig.getAtomFeedEntryLimit();

    private static final String PAGE_ABOUT = "about";

    private final EntryCache entries_;
    private final EntryTagCache tagCache_;
    private final EntryShadowCache shadowCache_;
    private final PageCache pages_;

    private final TwitterFeedHttpClient twitterClient_;
    private final StaticFileResolver staticResolver_;

    @Injectable
    public Blog(@Required final EntryCache entries,
                @Required final EntryTagCache tagCache,
                @Required final EntryShadowCache shadowCache,
                @Required final PageCache pages,
                @Required final TwitterFeedHttpClient twitterClient,
                @Required final StaticFileResolver staticResolver) {
        entries_ = entries;
        tagCache_ = tagCache;
        shadowCache_ = shadowCache;
        pages_ = pages;
        twitterClient_ = twitterClient;
        staticResolver_ = staticResolver;
    }

    // Pages

    @RequestMapping("^\\/$")
    public final Index index() {
        return new Index(entries_.getAll(entryLimitHomepage__));
    }
    @RequestMapping("^\\/about$")
    public final Page about() {
        return pages_.getPage(PAGE_ABOUT);
    }

    // Blog posts/entries

    @RequestMapping("^\\/((?!about$)(?<name>[a-zA-Z_0-9\\-]+))$")
    public final Entry entry(@Path("name") final String name) {
        return entries_.get(name);
    }

    @RequestMapping("^\\/tagged\\/(?<tag>.+)$")
    public final Tagged tagged(@Path("tag") final String tag) {
        final PagedContent<Entry> tagged = tagCache_.getAllTagged(tag);
        // If no content was found with the given tag, bail early; this results in a clean
        // 404 Not Found page.
        if (tagged.getContent().isEmpty()) {
            throw new ContentNotFoundException("Found no content tagged with: " + tag);
        }
        return new Tagged(tag, tagged);
    }

    // Static resources

    @RequestMapping("^(?<resource>\\/static\\/[a-zA-Z_0-9\\-\\.\\/]+)$")
    public final File staticFile(@Path("resource") final String resource) {
        return staticResolver_.getStaticFileInContentRoot(resource);
    }

    // Templatized content

    @RequestMapping("^\\/atom\\.xml$")
    public final AtomRss atomFeed() {
        return entries_.getAtomFeed(entryLimitAtomFeed__);
    }
    @RequestMapping("^\\/sitemap\\.xml$")
    public final Sitemap sitemap() {
        return entries_.getSitemap();
    }
    @RequestMapping("^\\/robots\\.txt$")
     public final RobotsTxt robots() {
        return new RobotsTxt();
    }
    @RequestMapping("^\\/humans\\.txt$")
    public final HumansTxt humans() {
        return new HumansTxt();
    }

    // APIs

    @RequestMapping("^\\/api\\/blog\\.json$")
    public final PagedContent<Entry> jsonFeed(@Query("before") final String commit) {
        return shadowCache_.getAllBefore(commit, entryLimitLoadMore__);
    }
    @RequestMapping("^\\/api\\/tweets\\.json$")
    public final TwitterFeed tweets() {
        return twitterClient_.getTweets();
    }

}
