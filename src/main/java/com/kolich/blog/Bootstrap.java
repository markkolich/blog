package com.kolich.blog;

import com.kolich.curacao.CuracaoDispatcherServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public final class Bootstrap extends CuracaoDispatcherServlet {

    private static final long serialVersionUID = 3191214230966342034L;

    @Override
    public final void myInit(final ServletConfig servletConfig,
        final ServletContext context) throws ServletException {
        
    }

    @Override
    public void myDestroy() {

    }

}
