package com.kolich.blog.entities.html;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.kolich.curacao.entities.AppendableCuracaoEntity;
import org.apache.commons.codec.binary.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.copyLarge;

public final class Utf8CompressedHtmlEntity extends AppendableCuracaoEntity {

    private static final String HTML_UTF_8_STRING = HTML_UTF_8.toString();

    private final int status_;
    private final String html_;

    public Utf8CompressedHtmlEntity(final int status,
                                    final String html) {
        super();
        status_ = status;
        html_ = compressHtml(html);
    }

    public Utf8CompressedHtmlEntity(final int status,
                                    final byte[] html) {
        this(status, StringUtils.newStringUtf8(html));
    }

    public Utf8CompressedHtmlEntity(final int status,
                                    final ByteArrayOutputStream html) {
        this(status, html.toByteArray());
    }

    public Utf8CompressedHtmlEntity(final String html) {
        this(SC_OK, html);
    }

    public Utf8CompressedHtmlEntity(final byte[] html) {
        this(SC_OK, html);
    }

    public Utf8CompressedHtmlEntity(final ByteArrayOutputStream html) {
        this(SC_OK, html);
    }

    @Override
    public final int getStatus() {
        return status_;
    }

    @Override
    public final String getContentType() {
        return HTML_UTF_8_STRING;
    }

    @Override
    public final void toWriter(final Writer writer) throws Exception {
        try(final Reader reader = new StringReader(html_)) {
            copyLarge(reader, writer);
        }
    }

    private static final String compressHtml(final String uncompressed) {
        final HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
        return compressor.compress(uncompressed);
    }

}
