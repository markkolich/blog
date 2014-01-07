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
import com.kolich.blog.entities.gson.EntryList;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.parameters.Path;
import com.kolich.curacao.annotations.parameters.Query;
import com.kolich.curacao.annotations.parameters.RequestUri;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

@Controller
public final class Blog {

    private static final int entryLimit__ = ApplicationConfig.getEntryLimit();

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

    @GET("/")
    public final Index index() {
        return new Index(entries_.getEntries(entryLimit__));
    }

    @GET("/about")
    public final Page about() {
        return pages_.getPage("about");
    }

    @GET("/contact")
    public final Page contact() {
        return pages_.getPage("contact");
    }

    @GET("/static/**")
    public final File staticFile(@RequestUri final String uri) throws IOException {
        return staticResolver_.getStaticFileInContentRoot(uri);
    }

    @GET("/blog.json")
    public final EntryList entries(@Query("before") final String commit) {
        return new EntryList(entries_.getEntriesBefore(commit, entryLimit__));
    }

    @GET("/tweets.json")
    public final Future<TwitterFeed> tweets() throws Exception {
        return twitterClient_.getTweets();
    }

    @GET("/{name}/**")
    public final Entry entry(@Path("name") final String name) {
        return entries_.getEntry(name);
    }

}
