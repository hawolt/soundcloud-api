package com.hawolt.api;

import com.hawolt.SoundcloudInternal;
import com.hawolt.data.media.hydratable.Hydratable;
import com.hawolt.data.media.hydratable.impl.Playlist;
import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.hydratable.impl.User;
import com.hawolt.data.media.search.query.LazyTrackIterator;
import com.hawolt.data.media.search.query.impl.LikeQuery;
import com.hawolt.logger.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundCloud {

    public static Track loadTrack(String link) throws Exception {
        Hydratable result = SoundcloudInternal.load(link);
        if (result instanceof Track track) return track;
        throw new IllegalArgumentException("Link does not resolve to a track: " + link);
    }

    public static LazyTrackIterator loadPlaylist(String link) throws Exception {
        Hydratable result = SoundcloudInternal.load(link);
        if (result instanceof Playlist playlist) {
            List<Long> ids = playlist.getList();
            Logger.info("Playlist {} loaded with {} track IDs", link, ids.size());
            return new LazyTrackIterator(
                    ids,
                    playlist.getId(),
                    playlist.getSecret(),
                    playlist.getLoadReferenceTimestamp()
            );
        }
        throw new IllegalArgumentException("Link does not resolve to a playlist: " + link);
    }

    public static LazyTrackIterator loadLikes(String link) throws Exception {
        Hydratable result = SoundcloudInternal.load(link);
        if (result instanceof User user) {
            List<Long> ids = SoundcloudInternal.load(new LikeQuery(user.getUserId()));
            Logger.info("User {} likes loaded with {} track IDs", user.getPermalink(), ids.size());
            return new LazyTrackIterator(ids, 0, null, System.currentTimeMillis());
        }
        throw new IllegalArgumentException("Link does not resolve to a user: " + link);
    }

    public static void load(String link, Job callback) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            try {
                Hydratable result = SoundcloudInternal.load(link);
                if (result instanceof Track track) {
                    callback.onResult(link, new Result.SingleTrack(track));
                } else if (result instanceof Playlist playlist) {
                    List<Long> ids = playlist.getList();
                    Logger.info("Playlist {} loaded with {} track IDs", link, ids.size());
                    LazyTrackIterator iterator = new LazyTrackIterator(
                            ids,
                            playlist.getId(),
                            playlist.getSecret(),
                            playlist.getLoadReferenceTimestamp()
                    );
                    callback.onResult(link, new Result.TrackCollection(iterator));
                } else if (result instanceof User user) {
                    List<Long> ids = SoundcloudInternal.load(new LikeQuery(user.getUserId()));
                    Logger.info("User {} likes loaded with {} track IDs", user.getPermalink(), ids.size());
                    LazyTrackIterator iterator = new LazyTrackIterator(
                            ids, 0, null, System.currentTimeMillis()
                    );
                    callback.onResult(link, new Result.TrackCollection(iterator));
                } else {
                    callback.onFailure(link, new IllegalArgumentException("Unrecognized link: " + link));
                }
            } catch (Exception e) {
                callback.onFailure(link, e);
            }
        });
        service.shutdown();
    }
}
