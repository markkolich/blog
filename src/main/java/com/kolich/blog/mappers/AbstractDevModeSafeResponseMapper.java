package com.kolich.blog.mappers;

import com.kolich.blog.ApplicationConfig;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;

public abstract class AbstractDevModeSafeResponseMapper<T>
    extends RenderingResponseTypeMapper<T> {

    private static final boolean isDevMode__ = ApplicationConfig.isDevMode();

    private static final String CACHE_CONTROL_NO_CACHE =
        "no-store, no-cache, must-revalidate, post-check=0, pre-check=0";

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final T content) throws Exception {
        // When in 'development' mode we set this header to prevent
        // any browser or proxy caching of CSS, JavaScript, or images.
        // I like to press Cmd-R and see my changes reflected
        // immediately without worrying about browser caching and
        // nonstandard proxies.
        if(isDevMode__) {
            response.addHeader(CACHE_CONTROL, CACHE_CONTROL_NO_CACHE);
        }
        renderSafe(context, response, content);
    }

    public abstract void renderSafe(final AsyncContext context,
                                    final HttpServletResponse response,
                                    @Nonnull final T content) throws Exception;

}
