package com.kolich.blog.mappers.resources;

import com.kolich.curacao.entities.CuracaoEntity;

import java.io.OutputStream;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

public final class NotModifiedResponseEntity implements CuracaoEntity {

    @Override
    public final int getStatus() {
        return SC_NOT_MODIFIED;
    }

    @Override
    public final String getContentType() {
        return null;
    }

    @Override
    public void write(final OutputStream os) throws Exception {
        // Nothing, intentional.
    }

}
