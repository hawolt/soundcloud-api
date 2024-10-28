package com.hawolt.data.media.download;

import com.hawolt.data.media.hydratable.impl.track.Track;



public interface DownloadCallback {
    void onCompletion(Track track, byte[] b);

    void onFailure(Track track, int fragment);

    void onTrack(Track track, byte[] b);
}
