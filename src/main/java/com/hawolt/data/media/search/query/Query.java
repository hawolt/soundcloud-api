package com.hawolt.data.media.search.query;

import org.json.JSONObject;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;


public interface Query<T> {
    String getKeyword();

    String checksum();

    Predicate<T> filter();

    Function<JSONObject, Boolean> getBooleanSupplier();

    Function<JSONObject, T> getTransformer();
}
