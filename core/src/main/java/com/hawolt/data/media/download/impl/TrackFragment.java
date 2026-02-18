package com.hawolt.data.media.download.impl;

import com.hawolt.data.media.MediaLoader;
import com.hawolt.data.media.download.FileCallback;
import com.hawolt.data.media.download.IFile;
import com.hawolt.ionhttp.request.IonResponse;
import com.hawolt.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class TrackFragment implements Runnable, IFile {

    private final AtomicInteger failures = new AtomicInteger(0);
    private final FileCallback callback;
    private volatile byte[] b;
    private final String url;
    private final int index;

    public TrackFragment(FileCallback callback, int index, String url) {
        this.callback = callback;
        this.index = index;
        this.url = url;
    }

    public byte[] bytes() {
        return b;
    }

    @Override
    public void run() {
        try {
            MediaLoader loader = new MediaLoader(url);
            try (IonResponse response = loader.call()) {
                this.b = response.body();
            }
            callback.onAssembly(index, this);
        } catch (Exception e) {
            Logger.error(e);
            callback.onFailure(index, failures.getAndIncrement(), url);
        }
    }
}