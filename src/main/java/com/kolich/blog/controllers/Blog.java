package com.kolich.blog.controllers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.blog.components.TwitterFeedHttpClient;
import com.kolich.blog.components.TwitterFeedHttpClient.TwitterFeedEntity;
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
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import java.util.concurrent.Future;

import static com.google.common.base.Charsets.UTF_8;

@Controller
public final class Blog {

    private static final int entryLimit__ = ApplicationConfig.getEntryLimit();

    private final EntryCache entries_;
    private final PageCache pages_;

    private final AsyncHttpClient twitterClient_;

    @Injectable
    public Blog(final EntryCache entries,
                final PageCache pages,
                final TwitterFeedHttpClient twitterClient) {
        entries_ = entries;
        pages_ = pages;
        twitterClient_ = twitterClient.getClient();
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

    @GET("/blog.json")
    public final EntryList entries(@Query("before") final String commit) {
        return new EntryList((commit == null) ?
            entries_.getEntries(entryLimit__) :
            entries_.getEntriesBefore(commit, entryLimit__));
    }

    @GET("/tweets.json")
    public final Future<TwitterFeedEntity> tweets() throws Exception {
        return twitterClient_.prepareGet("http://regatta:8080/twitter-feed/api/feed/markkolich.json")
            .execute(new AsyncCompletionHandler<TwitterFeedEntity>() {
                @Override
                public TwitterFeedEntity onCompleted(final Response response) throws Exception {
                    return new TwitterFeedEntity(response.getResponseBody(UTF_8.toString()));
                }
                @Override
                public void onThrowable(final Throwable t) {

                }
            });
    }

    @GET("/{name}/**")
    public final Entry entry(@Path("name") final String name) {
        return entries_.getEntry(name);
    }

}
