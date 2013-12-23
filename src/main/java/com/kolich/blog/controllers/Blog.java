package com.kolich.blog.controllers;

import com.kolich.blog.components.cache.EntryCache;
import com.kolich.blog.components.cache.IndexCache;
import com.kolich.blog.components.cache.PageCache;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.EntryList;
import com.kolich.blog.entities.Index;
import com.kolich.blog.entities.Page;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.parameters.Path;

@Controller
public final class Blog {

    private final IndexCache indexes_;
    private final EntryCache entries_;
    private final PageCache pages_;

    @Injectable
    public Blog(final IndexCache indexes,
                final EntryCache entries,
                final PageCache pages) {
        indexes_ = indexes;
        entries_ = entries;
        pages_ = pages;
    }

    @GET("/")
    public final Index index() {
        return indexes_.getIndex("home");
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
    public final EntryList getEntries() {
        return entries_.getEntryList();
    }

    @GET("/{name}/**")
    public final Entry entry(@Path("name") final String name) {
        return entries_.getEntry(name);
    }

}
