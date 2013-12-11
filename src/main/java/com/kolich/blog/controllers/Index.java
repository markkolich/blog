package com.kolich.blog.controllers;

import com.kolich.blog.git.BlogRepository;
import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.Injectable;
import com.kolich.curacao.annotations.methods.GET;
import com.kolich.curacao.annotations.parameters.Path;

@Controller
public final class Index {

    private final BlogRepository repository_;

    @Injectable
    public Index(final BlogRepository repository) {
        repository_ = repository;
    }

    @GET("/")
    public final String index() {
        return repository_.getContentRepository().toString();
    }

    @GET("/{content}/**")
    public final String content(@Path("content") final String content) {
        return content;
    }

}
