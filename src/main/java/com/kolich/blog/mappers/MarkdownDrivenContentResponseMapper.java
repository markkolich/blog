package com.kolich.blog.mappers;

import com.google.common.base.Charsets;
import com.kolich.blog.entities.MarkdownContent;
import com.kolich.blog.entities.MarkdownContentWithHeaderAndFooter;
import com.kolich.blog.entities.MarkdownFile;
import com.kolich.blog.exceptions.ContentRenderException;
import com.kolich.blog.mappers.handlers.ContentNotFoundExceptionHandler;
import com.kolich.curacao.annotations.mappers.ControllerReturnTypeMapper;
import com.kolich.curacao.entities.mediatype.AbstractBinaryContentTypeCuracaoEntity;
import com.kolich.curacao.handlers.responses.mappers.RenderingResponseTypeMapper;
import org.apache.commons.io.IOUtils;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.google.common.net.MediaType.HTML_UTF_8;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.slf4j.LoggerFactory.getLogger;

@ControllerReturnTypeMapper(MarkdownContentWithHeaderAndFooter.class)
public final class MarkdownDrivenContentResponseMapper
    extends RenderingResponseTypeMapper<MarkdownContentWithHeaderAndFooter> {

    private static final Logger logger__ =
        getLogger(MarkdownDrivenContentResponseMapper.class);

    @Override
    public final void render(final AsyncContext context,
                             final HttpServletResponse response,
                             @Nonnull final MarkdownContentWithHeaderAndFooter md) throws Exception {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append(fileToString(md.getHeader()));
            sb.append(markdownToString(md.getContent()));
            sb.append(fileToString(md.getFooter()));
            final String html = TemplateEngine.process(md, sb.toString());
            RenderingResponseTypeMapper.renderEntity(response,
                new Utf8HtmlEntity(html));
        } catch (Exception e) {
            logger__.debug("Content rendering exception: " + md, e);
            new ContentNotFoundExceptionHandler().render(context, response);
        }
    }

    public static final String markdownToString(final MarkdownFile mdf)
        throws Exception {
        final PegDownProcessor p = new PegDownProcessor(Extensions.ALL);
        return p.markdownToHtml(fileToString(mdf.getFile()));
    }

    public static final String fileToString(final File file) {
        try(final InputStream is = new FileInputStream(file)) {
            return IOUtils.toString(is, Charsets.UTF_8);
        } catch (Exception e) {
            throw new ContentRenderException("Failed to read file: " + file, e);
        }
    }

    private static final class TemplateEngine {

        private static final Pattern NAME = compile(quote("@{name}"));
        private static final Pattern TITLE = compile(quote("@{title}"));
        private static final Pattern HASH = compile(quote("@{hash}"));
        private static final Pattern DATE = compile(quote("@{date}"));

        private abstract static class Razor {
            public final Pattern p_;
            public Razor(final Pattern p) {
                p_ = p;
            }
            public abstract String getReplacement(final MarkdownContent md);
        }

        private static final List<Razor> razors__ = Arrays.asList(
            new Razor(NAME) {
                @Override
                public String getReplacement(final MarkdownContent md) {
                    return md.getName();
                }
            },
            new Razor(TITLE) {
                @Override
                public String getReplacement(final MarkdownContent md) {
                    return md.getTitle();
                }
            },
            new Razor(HASH) {
                @Override
                public String getReplacement(final MarkdownContent md) {
                    return md.getHash();
                }
            },
            new Razor(DATE) {
                @Override
                public String getReplacement(final MarkdownContent md) {
                    return md.getDateFormatted();
                }
            }
        );

        public static final String process(final MarkdownContent md,
                                           final String input) {
            String result = input;
            for(final Razor razor : razors__) {
                result = razor.p_.matcher(result).replaceAll(
                    razor.getReplacement(md));
            }
            return result;
        }

    }

    private static final class Utf8HtmlEntity
        extends AbstractBinaryContentTypeCuracaoEntity {

        public Utf8HtmlEntity(final String html) {
            super(SC_OK, HTML_UTF_8, getBytesUtf8(html));
        }

    }

}
