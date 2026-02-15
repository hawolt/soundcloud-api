package com.hawolt.api;

import com.hawolt.data.media.hydratable.impl.playlist.Playlist;
import com.hawolt.data.media.hydratable.impl.track.Track;
import com.hawolt.logger.Logger;

import java.util.*;

public class PlaylistTracker {

    private final Map<Long, Track> tracks;
    private final Playlist playlist;
    private final String link;
    private final int size;

    public PlaylistTracker(String link, Playlist source) {
        this.size = source.getList().size();
        this.tracks = new HashMap<>();
        this.playlist = source;
        this.link = link;
    }

    public void add(Track track) {
        this.tracks.put(track.getId(), track);
    }

    public int getCurrentSize() {
        return tracks.size();
    }

    public boolean isComplete() {
        boolean complete = tracks.size() >= size;
        if (complete) Logger.info("Done {}", link);
        return complete;
    }

    public int getTargetSize() {
        return playlist.getList().size();
    }

    public List<Track> getTrackList() {
        List<Track> list = new LinkedList<>();
        for (Long id : playlist.getList()) {
            list.add(tracks.get(id));
        }
        return list;
    }
}
