package com.hawolt.api;

public interface Action<T> {
    void handle(T t, String id, String... args);
}
