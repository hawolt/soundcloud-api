package com.hawolt.data.media.search.query;

import com.hawolt.data.media.search.Explorer;



public abstract class ObjectCollection<T> implements Iterable<T> {
    protected final Explorer<T> explorer;

    public ObjectCollection(Explorer<T> explorer) {
        this.explorer = explorer;
    }
}