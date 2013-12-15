package com.kolich.blog.entities;

public final class Entry {

    private final String name_;

    public Entry(final String name) {
        name_ = name;
    }

    public final String getName() {
        return name_;
    }

}
