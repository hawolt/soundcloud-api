package com.hawolt.data.media.search.query.impl;

import com.hawolt.data.SHA256;
import com.hawolt.data.media.hydratable.impl.user.User;
import com.hawolt.data.media.search.query.Query;
import org.json.JSONObject;

import java.util.function.Function;
import java.util.function.Predicate;



public class FollowingQuery implements Query<User> {
    private final long timestamp;
    private final long userId;

    public FollowingQuery(long timestamp, long userId) {
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public FollowingQuery(long userId) {
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
    public Predicate<User> filter() {
        return track -> true;
    }

    @Override
    public Function<JSONObject, User> getTransformer() {
        return object -> new User(timestamp, object);
    }
}
