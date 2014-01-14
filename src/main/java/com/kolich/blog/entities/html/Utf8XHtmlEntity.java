package com.kolich.blog.entities.html;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import org.apache.commons.codec.binary.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static com.google.common.net.MediaType.XML_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.copyLarge;

public final class Utf8XHtmlEntity extends AppendableCuracaoEntity {

    private static final String HTML_UTF_8_STRING = HTML_UTF_8.toString();
    private static final String XML_UTF_8_STRING = XML_UTF_8.toString();

    public static enum HtmlEntityType {
        HTML(HTML_UTF_8_STRING), XML(XML_UTF_8_STRING);
        private final String contentType_;
        private HtmlEntityType(final String contentType) {
            contentType_ = contentType;
        }
        public final String getType() {
            return contentType_;
        }
    }

    private final HtmlEntityType type_;
    private final int status_;
    private final String body_;

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final int status,
                           final String body) {
        super();
        type_ = type;
        status_ = status;
        body_ = compressHtml(body);
    }

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final int status,
                           final byte[] body) {
        this(type, status, StringUtils.newStringUtf8(body));
    }

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final int status,
                           final ByteArrayOutputStream body) {
        this(type, status, body.toByteArray());
    }

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final String body) {
        this(type, SC_OK, body);
    }

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final byte[] body) {
        this(type, SC_OK, body);
    }

    public Utf8XHtmlEntity(final HtmlEntityType type,
                           final ByteArrayOutputStream body) {
        this(type, SC_OK, body);
    }

    @Override
    public final int getStatus() {
        return status_;
    }

    @Override
    public final String getContentType() {
        return type_.getType();
    }

    @Override
    public final void toWriter(final Writer writer) throws Exception {
        try(final Reader reader = new StringReader(body_)) {
            copyLarge(reader, writer);
        }
    }

    private static final String compressHtml(final String uncompressed) {
        final HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
        return compressor.compress(uncompressed);
    }

}
