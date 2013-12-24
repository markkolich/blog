package com.kolich.blog.entities;

import com.google.gson.annotations.SerializedName;
import com.kolich.blog.entities.gson.GsonAppendableBlogEntity;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EntryList extends GsonAppendableBlogEntity {

    @SerializedName("entries")
    private final Set<Entry> entries_;

    public EntryList(@Nonnull final Set<Entry> entries) {
        entries_ = checkNotNull(entries, "Entry collection cannot be null.");
    }

}
