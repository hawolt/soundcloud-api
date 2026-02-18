package com.hawolt.api;

public interface Job {
    void onResult(String link, Result result);

    void onFailure(String link, Exception e);
}
