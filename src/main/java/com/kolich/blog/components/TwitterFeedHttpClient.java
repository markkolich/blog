package com.kolich.blog.components;

import com.kolich.curacao.annotations.Component;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import com.kolich.curacao.handlers.components.CuracaoComponent;
import com.ning.http.client.AsyncHttpClient;

import javax.servlet.ServletContext;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import static com.google.common.net.MediaType.JSON_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.copyLarge;

@Component
public class TwitterFeedHttpClient implements CuracaoComponent {

    private final AsyncHttpClient asyncHttpClient_;

    public TwitterFeedHttpClient() {
        asyncHttpClient_ = new AsyncHttpClient();
    }

    public final AsyncHttpClient getClient() {
        return asyncHttpClient_;
    }

    @Override
    public final void initialize(final ServletContext context) throws Exception {
        // Nothing, intentional.
    }

    @Override
    public final void destroy(final ServletContext context) throws Exception {
        asyncHttpClient_.close();
    }

    public static final class TwitterFeedEntity extends AppendableCuracaoEntity {

        private static final String JSON_UTF_8_TYPE = JSON_UTF_8.toString();

        private final String jsonString_;

        public TwitterFeedEntity(final String jsonString) {
            jsonString_ = jsonString;
        }

        @Override
        public final void toWriter(final Writer writer) throws Exception {
            try(final Reader reader = new StringReader(jsonString_)) {
                copyLarge(reader, writer);
            }
        }

        @Override
        public final int getStatus() {
            return SC_OK;
        }

        @Override
        public final String getContentType() {
            return JSON_UTF_8_TYPE;
        }

    }

}
