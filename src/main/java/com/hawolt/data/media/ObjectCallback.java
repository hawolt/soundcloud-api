package com.hawolt.data.media;



public interface ObjectCallback<T> {
    void ping(String link, T t, String... args);
}
