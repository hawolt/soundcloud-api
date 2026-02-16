package com.hawolt;

import com.hawolt.data.media.MediaInterface;
import com.hawolt.data.media.MediaLoader;
import com.hawolt.data.media.download.TrackFile;
import com.hawolt.data.media.hydratable.Hydratable;
import com.hawolt.data.media.hydratable.Hydration;
import com.hawolt.data.media.hydratable.impl.Playlist;
import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.hydratable.impl.User;
import com.hawolt.data.media.search.Explorer;
import com.hawolt.data.media.search.query.CompleteObjectCollection;
import com.hawolt.data.media.search.query.Query;
import com.hawolt.ionhttp.request.IonResponse;
import com.hawolt.logger.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class SoundcloudInternal {
    private static final Map<String, MediaInterface<? extends Hydratable>> MAPPING = new HashMap<>();

    static {
        MAPPING.put("user", User::new);
        MAPPING.put("playlist", Playlist::new);
        MAPPING.put("sound", (timestamp, object) -> new Track(timestamp, object.getJSONObject("data")));
    }

    public static Hydratable load(String source) throws Exception {
        String link = source.split("\\?")[0];
        Logger.debug("Load {}", link);
        MediaLoader loader = new MediaLoader(link);
        IonResponse response = loader.call();
        JSONArray hydration = Hydration.from(response);
        Map<String, JSONObject> available = new HashMap<>();
        for (int i = 0; i < hydration.length(); i++) {
            JSONObject object = hydration.getJSONObject(i);
            if (!object.has("hydratable")) continue;
            String hydratable = object.getString("hydratable");
            if (!MAPPING.containsKey(hydratable)) continue;
            available.put(hydratable, object);
        }
        String hydratable = available.containsKey("sound") ? "sound"
                : available.containsKey("playlist") ? "playlist"
                : available.containsKey("user") ? "user"
                : null;
        if (hydratable == null) return null;
        return MAPPING.get(hydratable).convert(System.currentTimeMillis(), available.get(hydratable));
    }

    public static List<Long> load(Query<Track> query) throws Exception {
        CompleteObjectCollection<Track> collection = Explorer.search(query);
        List<Long> ids = new LinkedList<>();
        for (Track track : collection) {
            ids.add(track.getId());
        }
        return ids;
    }
}
