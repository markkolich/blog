package com.kolich.blog;

import com.kolich.curacao.CuracaoDispatcherServlet;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public final class BootstrapServlet extends CuracaoDispatcherServlet {

    private static final long serialVersionUID = 3291215230966342074L;

    private static final Logger logger__ = getLogger(BootstrapServlet.class);

    @Override
    public void myInit(final ServletConfig servletConfig,
                       final ServletContext context) throws ServletException {
        logger__.info("Blog application service started.");
    }

    @Override
    public void myDestroy() {
        logger__.info("Blog application service stopped.");
    }

}
