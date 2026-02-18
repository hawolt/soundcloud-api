package com.hawolt.api;

import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.search.query.LazyTrackIterator;

public abstract class Result {

    public static final class SingleTrack extends Result {
        private final Track track;

        public SingleTrack(Track track) {
            this.track = track;
        }

        public Track getTrack() {
            return track;
        }
    }

    public static final class TrackCollection extends Result {
        private final LazyTrackIterator iterator;

        public TrackCollection(LazyTrackIterator iterator) {
            this.iterator = iterator;
        }

        public LazyTrackIterator getIterator() {
            return iterator;
        }
    }
}