package com.kolich.blog.entities.gson;

import com.google.gson.annotations.SerializedName;
import com.kolich.blog.entities.MarkdownContent;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PagedContent<T extends MarkdownContent>
    extends GsonAppendableBlogEntity {

    @SerializedName("content")
    private final List<T> content_;

    @SerializedName("remaining")
    private final int remaining_;

    public PagedContent(@Nonnull final List<T> content,
                        final int remaining) {
        content_ = checkNotNull(content, "Content cannot be null.");
        remaining_ = remaining;
    }

    public final List<T> getContent() {
        return content_;
    }

    public final int getRemaining() {
        return remaining_;
    }

}
