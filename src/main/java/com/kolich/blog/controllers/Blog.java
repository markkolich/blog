package com.kolich.blog.controllers;

import com.kolich.blog.components.GitRepository;
import com.kolich.blog.entities.Entry;
import com.kolich.blog.entities.Page;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.parameters.Path;

@Controller
public final class Blog {

    private final GitRepository git_;

    @Injectable
    public Blog(final GitRepository git) {
        git_ = git;
    }

    @GET("/")
    public final String index() {
        return "Home page";
    }

    /*
    @GET("/about")
    public final Page about() {
        return new Page("about");
    }

    @GET("/contact")
    public final Page contact() {
        return new Page("contact");
    }
    */

    @GET("/{name}/**")
    public final Entry entry(@Path("name") final String name) {
        return git_.getEntry(name);
    }

}
