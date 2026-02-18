package com.hawolt.data.media.track;

import com.hawolt.data.VirtualClient;
import com.hawolt.data.media.MediaLoader;
import com.hawolt.data.media.download.DownloadCallback;
import com.hawolt.data.media.download.TrackFile;
import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.ionhttp.request.IonResponse;
import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

public class MP3 {

    private static final String TARGET_PROTOCOL = "hls";

    private final String authorization;
    private final EXTM3U extm3u;
    private final Track track;

    public static MP3 load(Track track, String authorization, Transcoding... transcodings) {
        try {
            return new MP3(track, authorization, transcodings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load MP3 for track " + track.getId(), e);
        }
    }

    private MP3(Track track, String authorization, Transcoding... transcodings) throws Exception {
        this.track = track;
        this.authorization = authorization;
        this.extm3u = resolveHLSStream(transcodings);
    }

    private EXTM3U resolveHLSStream(Transcoding... transcodings) throws Exception {
        for (Transcoding transcoding : transcodings) {
            if (!TARGET_PROTOCOL.equalsIgnoreCase(transcoding.getProtocol())) {
                continue;
            }

            String resource = buildStreamUrl(transcoding);
            Logger.debug("stream for track {} at resource {}", track.getId(), resource);

            MediaLoader loader = new MediaLoader(resource);
            try (IonResponse response = loader.call()) {
                JSONObject object = new JSONObject(new String(response.body(), StandardCharsets.UTF_8));
                if (!object.has("url")) {
                    continue;
                }
                return new EXTM3U(object.getString("url"));
            }
        }
        return null;
    }

    private String buildStreamUrl(Transcoding transcoding) throws Exception {
        String client = "client_id" + "=" + VirtualClient.getID();
        String auth = "track_authorization" + "=" + authorization;
        String parameters = client + "&" + auth;
        String separator = transcoding.getUrl().contains("?") ? "&" : "?";
        return transcoding.getUrl() + separator + parameters;
    }

    public Track getTrack() {
        return track;
    }

    public String getAuthorization() {
        return authorization;
    }

    public EXTM3U getEXTM3U() {
        return extm3u;
    }

    public boolean hasStream() {
        return extm3u != null;
    }

    public void download(DownloadCallback callback) {
        TrackFile.create(callback, this);
    }

    public void download(DownloadCallback callback, ExecutorService service) {
        TrackFile.create(callback, this, service);
    }
}