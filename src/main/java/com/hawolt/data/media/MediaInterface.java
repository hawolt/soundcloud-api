package com.hawolt.data.media;

import com.hawolt.data.media.hydratable.Hydratable;
import org.json.JSONObject;



public interface MediaInterface<T extends Hydratable> {
    T convert(long timestamp, JSONObject object);
}
