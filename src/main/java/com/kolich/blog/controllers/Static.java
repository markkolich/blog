package com.kolich.blog.controllers;

import com.kolich.curacao.annotations.Controller;
import com.kolich.curacao.annotations.methods.GET;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public final class Static {

    @GET("/static/**")
    public final void forwarder(final ServletContext sContext,
                                final AsyncContext context,
                                final HttpServletRequest request,
                                final HttpServletResponse response) throws Exception {
        try {
            final RequestDispatcher dispatcher = sContext.getNamedDispatcher("default");
            dispatcher.forward(request, response);
        } finally {
            context.complete();
        }
    }

}
