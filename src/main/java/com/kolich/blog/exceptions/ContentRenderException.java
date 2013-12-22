package com.kolich.blog.exceptions;

import com.kolich.curacao.exceptions.routing.CuracaoRoutingException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public final class ContentRenderException extends CuracaoRoutingException {

    private static final long serialVersionUID = -1303687433497348365L;

    public ContentRenderException(String message, final Exception cause) {
        super(SC_NOT_FOUND, message, cause);
    }

}
