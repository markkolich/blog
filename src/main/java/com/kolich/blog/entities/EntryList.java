package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;
import com.kolich.blog.entities.gson.GsonAppendableBlogEntity;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EntryList extends GsonAppendableBlogEntity {

    @SerializedName("entries")
    private final Collection<Entry> entries_;

    public EntryList(@Nonnull final Collection<Entry> entries) {
        entries_ = checkNotNull(entries, "Entry collection cannot be null.");
    }

}
