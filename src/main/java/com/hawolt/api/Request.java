package com.hawolt.api;

public interface Request<T> {
    void accept(T result);
}
