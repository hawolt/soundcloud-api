package com.hawolt.api;

public interface Reporter {
    void onPlaylistSize(String source, int size);

    void onProgress(String source, int current, int target);
}
