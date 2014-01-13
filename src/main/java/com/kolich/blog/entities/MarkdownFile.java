package com.kolich.blog.entities;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.kolich.blog.exceptions.ContentRenderException;

import java.io.File;
import java.lang.reflect.Type;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.blog.mappers.MarkdownDrivenContentResponseMapper.markdownToString;

public final class MarkdownFile {

    private final transient File file_;

    public MarkdownFile(final File file) {
        file_ = checkNotNull(file, "Markdown file cannot be null.");
    }

    public final File getFile() {
        return file_;
    }

    @Override
    public final String toString() {
        return file_.getAbsolutePath();
    }

    public static final class MarkdownFileGsonAdapter
        implements JsonSerializer<MarkdownFile> {

        @Override
        public final JsonElement serialize(final MarkdownFile src,
                                           final Type typeOfSrc,
                                           final JsonSerializationContext context) {
            try {
                return new JsonPrimitive(markdownToString(src));
            } catch (Exception e) {
                throw new ContentRenderException("Failed to serialize " +
                    "markdown file: " + src.getFile(), e);
            }
        }

    }

}
