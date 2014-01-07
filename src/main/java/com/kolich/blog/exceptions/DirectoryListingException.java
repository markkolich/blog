package com.kolich.blog.exceptions;

import com.kolich.curacao.exceptions.routing.CuracaoRoutingException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public final class DirectoryListingException extends CuracaoRoutingException {

    private static final long serialVersionUID = -1303687433497348365L;

    public DirectoryListingException(String message) {
        super(SC_NOT_FOUND, message, null);
    }

}
