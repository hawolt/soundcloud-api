package com.hawolt.api;

import com.hawolt.data.media.hydratable.impl.playlist.Playlist;
import com.hawolt.data.media.hydratable.impl.track.Track;
import com.hawolt.data.media.search.query.CompleteObjectCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Job {
    private final Map<String, PlaylistTracker> cache;
    private final Map<String, List<Track>> loaded;
    private final Request<Job> request;
    private final Reporter reporter;
    private final String[] targets;
    private final String id;

    public Job(String id, Request<Job> request, Reporter reporter, String... targets) {
        this.loaded = new HashMap<>();
        this.cache = new HashMap<>();
        this.reporter = reporter;
        this.request = request;
        this.targets = targets;
        this.id = id;
    }

    public Job(String id, Request<Job> request, String... targets) {
        this(id, request, null, targets);
    }

    private void check() {
        if (loaded.size() != targets.length) return;
        this.request.accept(this);
    }

    public void add(String playlist, Track track) {
        PlaylistTracker tracker = cache.get(playlist);
        tracker.add(track);
        if (reporter != null) reporter.onProgress(playlist, tracker.getCurrentSize(), tracker.getTargetSize());
        if (!tracker.isComplete()) return;
        this.loaded.put(playlist, tracker.getTrackList());
        this.check();
    }

    public void complete(String link, CompleteObjectCollection<Track> collection) {
        this.loaded.put(link, collection.getList());
        this.check();
    }

    public void update(String link, Playlist playlist) {
        if (reporter != null) reporter.onPlaylistSize(link, playlist.getList().size());
        this.cache.put(link, new PlaylistTracker(link, playlist));
    }

    public String getUUID() {
        return id;
    }

    public Map<String, List<Track>> getJobResult() {
        return loaded;
    }

    public static Job create(String id, Request<Job> request, String... targets) {
        return create(id, request, null, targets);
    }

    public static Job create(String id, Request<Job> request, Reporter reporter, String... targets) {
        return new Job(id, request, reporter, targets);
    }
}
