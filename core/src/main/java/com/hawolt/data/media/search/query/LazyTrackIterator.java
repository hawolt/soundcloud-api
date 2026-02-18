package com.hawolt.data.media.search.query;

import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.search.Explorer;
import com.hawolt.data.media.search.query.impl.TrackQuery;
import com.hawolt.logger.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


public class LazyTrackIterator implements Iterator<Track>, Iterable<Track> {

    private final List<Long> trackIds;
    private final long playlistId;
    private final String secret;
    private final long timestamp;
    private int index;

    public LazyTrackIterator(List<Long> trackIds, long playlistId, String secret, long timestamp) {
        this.playlistId = playlistId;
        this.timestamp = timestamp;
        this.trackIds = trackIds;
        this.secret = secret;
        this.index = 0;
    }

    public int size() {
        return trackIds.size();
    }

    public int remaining() {
        return trackIds.size() - index;
    }

    @Override
    public boolean hasNext() {
        return index < trackIds.size();
    }

    @Override
    public Track next() {
        if (!hasNext()) throw new NoSuchElementException();
        long id = trackIds.get(index++);
        try {
            TrackQuery query = new TrackQuery(timestamp, id, playlistId, secret);
            ObjectCollection<Track> collection = Explorer.browse(query);
            for (Track track : collection) {
                return track;
            }
            throw new RuntimeException("No track resolved for id: " + id);
        } catch (Exception e) {
            Logger.error("Failed to lazily load track {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to load track " + id, e);
        }
    }

    @Override
    public Iterator<Track> iterator() {
        return this;
    }
}