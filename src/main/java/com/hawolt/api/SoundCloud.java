package com.hawolt.api;

import com.hawolt.LoadCallback;
import com.hawolt.SoundcloudInternal;
import com.hawolt.data.media.ObjectCallback;
import com.hawolt.data.media.hydratable.impl.playlist.Playlist;
import com.hawolt.data.media.hydratable.impl.playlist.PlaylistManager;
import com.hawolt.data.media.hydratable.impl.track.Track;
import com.hawolt.data.media.hydratable.impl.track.TrackManager;
import com.hawolt.data.media.hydratable.impl.user.User;
import com.hawolt.data.media.hydratable.impl.user.UserManager;
import com.hawolt.data.media.search.Explorer;
import com.hawolt.data.media.search.query.CompleteObjectCollection;
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

    public final Map<Long, Track> tracks = new HashMap<>();
    public final Map<String, Job> cache = new HashMap<>();

    private Action<Playlist> action = (playlist, uuid, args) -> {
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
                    if (tracks.containsKey(track.getId())) {
                        cache.get(uuid).add(args[0], track);
                    } else {
                        SoundcloudInternal.load(track.getLink(), uuid, args[0]);
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    };

    public final ObjectCallback<Track> trackObjectCallback = (link, track, args) -> {
        Job target = cache.get(args[0]);
        if (args.length > 1) {
            target.add(args[1], track);
        } else {
            CompleteObjectCollection<Track> collection = new CompleteObjectCollection<>();
            collection.getList().add(track);
            target.complete(link, collection);
        }
        tracks.put(track.getId(), track);
    };

    public final ObjectCallback<User> userObjectCallback = (link, user, args) -> {
        Query<Track> query;
        if (link.endsWith("likes")) {
            query = new LikeQuery(user.getUserId());
        } else {
            query = new UploadQuery(user.getUserId());
        }
        try {
            cache.get(args[0]).complete(link, Explorer.search(query));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

    public final ObjectCallback<Playlist> playlistObjectCallback = (link, playlist, args) -> {
        this.cache.get(args[0]).update(link, playlist);
        this.action.handle(playlist, args[0], link);
    };

    public void configure(Action<Playlist> action) {
        this.action = action;
    }

    public static String load(Request<Job> request, String... links) {
        return load(null, request, null, links);
    }

    public static String load(LoadCallback callback, Request<Job> request, Reporter reporter, String... links) {
        String id = UUID.randomUUID().toString();
        Job job = Job.create(id, request, reporter, links);
        SoundCloud instance = getInstance();
        instance.cache.put(id, job);
        for (String link : links) {
            Logger.info("Load {}", link);
            SoundcloudInternal.load(link, callback, id);
        }
        return id;
    }

    public static SoundCloud getInstance() {
        return INSTANCE;
    }
}
