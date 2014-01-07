package com.kolich.blog.mappers;

import com.google.common.net.MediaType;
import com.google.common.primitives.Ints;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@ControllerReturnTypeMapper(File.class)
public final class StaticFileResponseMapper
    extends RenderingResponseTypeMapper<File> {

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             final @Nonnull File entity) throws Exception {
        response.setContentLength(Ints.checkedCast(entity.length())); // Sigh
        renderEntity(response, new CuracaoEntity() {
            @Override
            public int getStatus() {
                return HttpServletResponse.SC_OK;
            }
            @Override
            public String getContentType() {
                return MediaType.parse(entity.getAbsolutePath()).toString();
            }
            @Override
            public void write(final OutputStream os) throws Exception {
                try(InputStream is = new FileInputStream(entity)) {
                    IOUtils.copyLarge(is, os);
                }
            }
        });
    }

}
