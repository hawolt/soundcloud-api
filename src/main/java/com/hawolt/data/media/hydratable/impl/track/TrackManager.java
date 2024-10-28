package com.hawolt.data.media.hydratable.impl.track;

import com.hawolt.data.media.ObjectCallback;
import com.hawolt.data.media.hydratable.HydratableInterface;



public class TrackManager implements HydratableInterface<Track> {
    private final ObjectCallback<Track> callback;

    public TrackManager(ObjectCallback<Track> callback) {
        this.callback = callback;
    }

    @Override
    public void accept(String link, Track hydratable, String... args) {
        callback.ping(link, hydratable, args);
    }
}
