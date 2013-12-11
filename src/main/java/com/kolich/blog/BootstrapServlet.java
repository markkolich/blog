package com.kolich.blog;

import com.kolich.curacao.CuracaoDispatcherServlet;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import static org.slf4j.LoggerFactory.getLogger;

public final class BootstrapServlet extends CuracaoDispatcherServlet {

    private static final long serialVersionUID = 3191214230966342034L;

    private static final Logger logger__ = getLogger(BootstrapServlet.class);

    @Override
    public final void myInit(final ServletConfig servletConfig,
        final ServletContext context) throws ServletException {
        logger__.info("Blog application service started.");
    }

    @Override
    public final void myDestroy() {
        logger__.info("Blog application service stopped.");
    }

}
