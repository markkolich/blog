package com.kolich.blog.components;

import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.handlers.components.CuracaoComponent;

import javax.servlet.ServletContext;

@Component
public final class ContextComponent implements CuracaoComponent {

    private ServletContext context_;

    @Override
    public void initialize(final ServletContext context) throws Exception {
        context_ = context;
    }

    @Override
    public void destroy(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

    public final ServletContext getContext() {
        return context_;
    }

}
