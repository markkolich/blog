package com.kolich.blog.entities.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kolich.blog.entities.MarkdownFile;
import com.kolich.common.entities.KolichCommonEntity;
import com.kolich.common.entities.gson.KolichDefaultDateTypeAdapter;
import com.kolich.curacao.gson.GsonAppendableCuracaoEntity;

import java.util.Date;

public abstract class GsonAppendableBlogEntity
    extends GsonAppendableCuracaoEntity {

    public GsonAppendableBlogEntity() {
        super(getNewBlogGsonInstance());
    }

    public static final GsonBuilder getNewBlogGsonBuilder() {
        return KolichCommonEntity.getDefaultGsonBuilder()
            .registerTypeAdapter(new TypeToken<Date>(){}.getType(),
                new KolichDefaultDateTypeAdapter(BlogContentDateFormat.getNewInstance()))
            .registerTypeAdapter(new TypeToken<MarkdownFile>(){}.getType(),
                new MarkdownFile.MarkdownFileGsonAdapter());
    }

    public static final Gson getNewBlogGsonInstance() {
        return getNewBlogGsonBuilder().create();
    }

}
