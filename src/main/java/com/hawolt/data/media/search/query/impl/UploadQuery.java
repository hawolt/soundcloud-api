package com.hawolt.data.media.search.query.impl;

import com.hawolt.data.SHA256;
import com.hawolt.data.media.hydratable.impl.track.Track;
import com.hawolt.data.media.search.query.Query;
import org.json.JSONObject;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;


public class UploadQuery implements Query<Track> {
    private final long timestamp;
    private final long userId;

    public UploadQuery(long timestamp, long userId) {
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public UploadQuery(long userId) {
        this(System.currentTimeMillis(), userId);
    }

    @Override
    public String getKeyword() {
        return String.valueOf(userId);
    }

    @Override
    public String checksum() {
        return SHA256.hash(String.join(getClass().getSimpleName(), String.valueOf(userId)));
    }

    @Override
    public Predicate<Track> filter() {
        return track -> true;
    }

    @Override
    public Function<JSONObject, Boolean> getBooleanSupplier() {
        return object -> true;
    }


    @Override
    public Function<JSONObject, Track> getTransformer() {
        return object -> new Track(timestamp, object);
    }
}
