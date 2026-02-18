package com.hawolt.data.media.search.query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;


public class PartialCollection<T> implements Iterable<T> {
    private final List<T> list = new ArrayList<>();

    public PartialCollection(Predicate<T> filter, Function<JSONObject, Boolean> supplier, Function<JSONObject, T> transformer) {
        this(filter, supplier, transformer, new JSONArray());
    }

    public PartialCollection(Predicate<T> filter, Function<JSONObject, Boolean> supplier, Function<JSONObject, T> transformer, JSONArray array) {
        for (int i = 0; i < array.length(); i++) {
            JSONObject reference = array.getJSONObject(i);
            if (supplier.apply(reference)) {
                T t = transformer.apply(reference);
                if (filter.test(t)) list.add(t);
            }
        }
    }

    public void append(T object) {
        list.add(object);
    }

    public List<T> getList() {
        return list;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}