package com.kolich.blog.exceptions;

import com.kolich.curacao.exceptions.CuracaoException;

public final class ContentNotFoundException extends CuracaoException {

    private static final long serialVersionUID = -1303687433497348365L;

    public ContentNotFoundException(final String message) {
        super(message);
    }

}
