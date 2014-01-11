package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import com.google.common.primitives.Ints;
import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.CuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;
import static com.kolich.blog.ApplicationConfig.getContentTypeForExtension;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.IOUtils.copyLarge;

@ControllerReturnTypeMapper(File.class)
public final class StaticFileResponseMapper
    extends RenderingResponseTypeMapper<File> {

    private static final boolean isDevMode__ = ApplicationConfig.isDevMode();

    private static final String CACHE_CONTROL_NO_CACHE =
        "no-store, no-cache, must-revalidate, post-check=0, pre-check=0";

    private static final String DEFAULT_CONTENT_TYPE =
        MediaType.OCTET_STREAM.toString();

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             final @Nonnull File entity) throws Exception {
        renderEntity(response, new CuracaoEntity() {
            @Override
            public int getStatus() {
                return SC_OK;
            }
            @Override
            public String getContentType() {
                // Normalize (important) and extract the extension from the file.
                final String extension = getExtension(normalize(
                    entity.getAbsolutePath()));
                final String contentType = getContentTypeForExtension(extension,
                    DEFAULT_CONTENT_TYPE);
                // Build a proper MediaType object representing the files true
                // Content-Type.  It really annoys me that 'contentType' below
                // isn't final, but it has to be this way given we need to set
                // the "charset=UTF-8" attribute on the type later if it's text.
                MediaType mediaType = MediaType.parse(contentType);
                // Is it text?  If so, we need to a proper UTF-8 charset attribute.
                if(isText(mediaType)) {
                    mediaType = mediaType.withCharset(Charsets.UTF_8);
                }
                return mediaType.toString();
            }
            @Override
            public void write(final OutputStream os) throws Exception {
                // When in 'development' mode we set this header to prevent
                // any browser or proxy caching of CSS, JavaScript, or images.
                // I like to press Cmd-R and see my changes reflected
                // immediately without worrying about browser caching and
                // nonstandard proxies.
                if(isDevMode__) {
                    response.addHeader(CACHE_CONTROL, CACHE_CONTROL_NO_CACHE);
                }
                // Sigh.  Yep, here we are "safely" casting a Long to an Integer.
                // This ~should~ be fine, in theory, given that this response
                // mapper is likely not going to serve up content larger than
                // 2GB.  If we do ever need to serve up crazy large files, this
                // will not work.  By that time, we'll probably be using Servlet
                // 3.1 which has a proper setContentLength(Long) method.
                response.setContentLength(Ints.checkedCast(entity.length()));
                try(InputStream is = new FileInputStream(entity)) {
                    copyLarge(is, os);
                }
            }
        });
    }

    /**
     * Returns true if the provided {@link MediaType} is "text", and false
     * otherwise.  More formally, if the provided {@link MediaType}'s type is
     * text, ignoring its subtype. Note that this method also returns true for
     * types that we want to be UTF-8, like JavaScript and JSON, but their
     * formal {@link MediaType} type is not "text".
     */
    private static final boolean isText(@Nonnull final MediaType type) {
        return type.is(MediaType.ANY_TEXT_TYPE) ||
            // JavaScript and JSON needs to be served up as "UTF-8" too.
            // Apparently rumor has it JSON (and JavaScript?) is considered
            // by most modern browsers to be UTF-8 by default, so this
            // special handling of "js" and "json" types might be unnecessary.
            // IDK, it certainly can't hurt and bytes are cheap.
            type.subtype().equals(MediaType.JAVASCRIPT_UTF_8.subtype()) ||
            type.subtype().equals(MediaType.JSON_UTF_8.subtype());
    }

}
