package com.kolich.blog.entities.html;

import com.kolich.curacao.entities.AppendableCuracaoEntity;
import org.apache.commons.codec.binary.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import static com.google.common.net.MediaType.XML_UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.io.IOUtils.copyLarge;

public final class Utf8XmlEntity extends AppendableCuracaoEntity {

    private static final String XML_UTF_8_STRING = XML_UTF_8.toString();

    private final int status_;
    private final String xml_;

    public Utf8XmlEntity(final int status, final String xml) {
        status_ = status;
        xml_ = xml;
    }

    public Utf8XmlEntity(final String xml) {
        this(SC_OK, xml);
    }

    public Utf8XmlEntity(final ByteArrayOutputStream xml) {
        this(StringUtils.newStringUtf8(xml.toByteArray()));
    }

    @Override
    public final void toWriter(final Writer writer) throws Exception {
        try(final Reader reader = new StringReader(xml_)) {
            copyLarge(reader, writer);
        }
    }

    @Override
    public final int getStatus() {
        return status_;
    }

    @Override
    public final String getContentType() {
        return XML_UTF_8_STRING;
    }

}
