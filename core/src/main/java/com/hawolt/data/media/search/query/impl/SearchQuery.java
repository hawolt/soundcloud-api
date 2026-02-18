package com.hawolt.data.media.search.query.impl;

import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.search.query.AdvancedQuery;
import org.json.JSONObject;

import java.util.function.Function;


public class SearchQuery extends AdvancedQuery {
    private final long timestamp;
    private final String keyword;

    public SearchQuery(long timestamp, String keyword) {
        this.timestamp = timestamp;
        this.keyword = keyword;
    }

    public SearchQuery(String keyword) {
        this(System.currentTimeMillis(), keyword);
    }

    @Override
    public String getKeyword() {
        return keyword;
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
