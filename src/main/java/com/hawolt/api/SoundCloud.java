package com.hawolt.api;

import com.hawolt.SoundcloudInternal;
import com.hawolt.data.media.ObjectCallback;
import com.hawolt.data.media.hydratable.impl.playlist.Playlist;
import com.hawolt.data.media.hydratable.impl.playlist.PlaylistManager;
import com.hawolt.data.media.hydratable.impl.track.Track;
import com.hawolt.data.media.hydratable.impl.track.TrackManager;
import com.hawolt.data.media.hydratable.impl.user.User;
import com.hawolt.data.media.hydratable.impl.user.UserManager;
import com.hawolt.data.media.search.Explorer;
import com.hawolt.data.media.search.query.ObjectCollection;
import com.hawolt.data.media.search.query.Query;
import com.hawolt.data.media.search.query.impl.LikeQuery;
import com.hawolt.data.media.search.query.impl.TrackQuery;
import com.hawolt.data.media.search.query.impl.UploadQuery;
import com.hawolt.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoundCloud {

    private static final SoundCloud INSTANCE = new SoundCloud();

    static {
        SoundcloudInternal.register(Playlist.class, new PlaylistManager(INSTANCE.playlistObjectCallback));
        SoundcloudInternal.register(Track.class, new TrackManager(INSTANCE.trackObjectCallback));
        SoundcloudInternal.register(User.class, new UserManager(INSTANCE.userObjectCallback));
    }

    private static final Map<Long, Track> tracks = new HashMap<>();
    private static final Map<String, Job> cache = new HashMap<>();

    private final ObjectCallback<Track> trackObjectCallback = (link, track, args) -> {
        SoundCloud.cache.get(args[0]).add(args[1], track);
        SoundCloud.tracks.put(track.getId(), track);
    };

    private final ObjectCallback<User> userObjectCallback = (link, user, args) -> {
        Query<Track> query;
        if (link.endsWith("likes")) {
            query = new LikeQuery(user.getUserId());
        } else {
            query = new UploadQuery(user.getUserId());
        }
        try {
            SoundCloud.cache.get(args[0]).complete(link, Explorer.search(query));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    private final ObjectCallback<Playlist> playlistObjectCallback = (link, playlist, args) -> {
        SoundCloud.cache.get(args[0]).update(link, playlist);
        for (long id : playlist) {
            try {
                TrackQuery query = new TrackQuery(
                        playlist.getLoadReferenceTimestamp(),
                        id,
                        playlist.getId(),
                        playlist.getSecret()
                );
                ObjectCollection<Track> collection = Explorer.browse(query);
                for (Track track : collection) {
                    if (SoundCloud.tracks.containsKey(track.getId())) {
                        SoundCloud.cache.get(args[0]).add(args[1], track);
                    } else {
                        SoundcloudInternal.load(track.getLink(), args[0], link);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    public static void load(Request<Job> request, String... links) {
        String id = UUID.randomUUID().toString();
        Job job = Job.create(request, links);
        SoundCloud.cache.put(id, job);
        for (String link : links) {
            Logger.info("Load {}", link);
            SoundcloudInternal.load(link, id);
        }
    }
}
