package com.hawolt.data.media.hydratable.impl;

import com.hawolt.data.media.MediaLoader;
import com.hawolt.data.media.download.FileManager;
import com.hawolt.data.media.hydratable.Hydratable;
import com.hawolt.data.media.track.MP3;
import com.hawolt.data.media.track.Media;
import com.hawolt.data.media.track.Tags;
import com.hawolt.data.media.track.User;
import com.hawolt.ionhttp.request.IonResponse;
import com.hawolt.logger.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class Track extends Hydratable {

    public static boolean debug = false;

    private static final long PRO_DURATION_THRESHOLD_MS = 10000L;

    private final long id;
    private final Media media;
    private final User user;
    private final Tags tags;

    private final String description;
    private final String title;
    private final String genre;
    private final String link;
    private final String permalink;
    private final String artwork;
    private final String authorization;
    private final String uri;
    private final String waveform;
    private final String secretToken;

    private final int likeCount;
    private final int commentCount;
    private final int playbackCount;
    private final int repostCount;

    private final long duration;
    private final long createdAt;
    private final long lastModified;
    private final long fullDuration;

    private final JSONObject source;

    public Track(long timestamp, long id) {
        super(timestamp);
        this.id = id;
        this.media = null;
        this.user = null;
        this.tags = null;
        this.description = "";
        this.title = "";
        this.genre = "";
        this.link = "";
        this.permalink = "";
        this.artwork = null;
        this.authorization = null;
        this.uri = "";
        this.waveform = null;
        this.secretToken = null;
        this.likeCount = 0;
        this.commentCount = 0;
        this.playbackCount = 0;
        this.repostCount = 0;
        this.duration = 0;
        this.createdAt = 0;
        this.lastModified = 0;
        this.fullDuration = 0;
        this.source = new JSONObject();
    }

    public Track(long timestamp, JSONObject o) {
        super(timestamp);
        this.id = getOrDefault(o, "id", 0L);
        this.secretToken = getNullableString(o, "secret_token");
        this.waveform = getNullableString(o, "waveform_url");
        this.authorization = getNullableString(o, "track_authorization");
        this.artwork = getNullableString(o, "artwork_url");
        this.media = new Media(o.getJSONObject("media"));
        this.user = new User(o.getJSONObject("user"));
        this.description = getOrDefault(o, "description", "");
        this.title = o.getString("title");
        this.genre = getOrDefault(o, "genre", "");
        this.permalink = getOrDefault(o, "permalink", "");
        this.uri = getOrDefault(o, "uri", "");
        this.link = o.getString("permalink_url");
        this.tags = new Tags(genre, o.getString("tag_list"));
        this.likeCount = getOrDefault(o, "likes_count", 0);
        this.playbackCount = getOrDefault(o, "playback_count", 0);
        this.repostCount = getOrDefault(o, "reposts_count", 0);
        this.commentCount = getOrDefault(o, "comment_count", 0);
        this.duration = getOrDefault(o, "duration", 0L);
        this.fullDuration = getOrDefault(o, "full_duration", 0L);
        this.createdAt = parseTimestamp(o, "created_at", System.currentTimeMillis());
        this.lastModified = parseTimestamp(o, "last_modified", 0L);
        this.source = o;
        if (debug) {
            Logger.debug("loaded metadata for track {} as {}", id, o);
        }
    }

    public byte[] loadArtwork() throws IOException {
        MediaLoader loader = new MediaLoader(artwork);
        try (IonResponse response = loader.call()) {
            return response.body();
        } catch (Exception e) {
            throw new IOException("Failed to load artwork for track " + title);
        }
    }

    private static String getNullableString(JSONObject o, String key) {
        return o.isNull(key) ? null : o.getString(key);
    }

    private static String getOrDefault(JSONObject o, String key, String defaultValue) {
        return o.isNull(key) ? defaultValue : o.getString(key);
    }

    private static int getOrDefault(JSONObject o, String key, int defaultValue) {
        return o.isNull(key) ? defaultValue : o.getInt(key);
    }

    private static long getOrDefault(JSONObject o, String key, long defaultValue) {
        return o.isNull(key) ? defaultValue : o.getLong(key);
    }

    private static long parseTimestamp(JSONObject o, String key, long defaultValue) {
        return o.isNull(key) ? defaultValue : Instant.parse(o.getString(key)).toEpochMilli();
    }

    public JSONObject getSource() {
        return source;
    }

    public MP3 getMP3() {
        return MP3.load(this, authorization, media.getTranscoding());
    }

    public boolean isCached() {
        return FileManager.isCached(this);
    }

    public File getFile() {
        return FileManager.getFile(this);
    }

    public boolean isPro() {
        return Math.abs(fullDuration - duration) > PRO_DURATION_THRESHOLD_MS;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public String getLink() {
        return link;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getArtwork() {
        return artwork;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getUri() {
        return uri;
    }

    public String getWaveformURL() {
        return waveform;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public String getDescription() {
        return description;
    }

    public Media getMedia() {
        return media;
    }

    public User getUser() {
        return user;
    }

    public Tags getTags() {
        return tags;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getPlaybackCount() {
        return playbackCount;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public long getDuration() {
        return duration;
    }

    public long getFullDuration() {
        return fullDuration;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track)) return false;
        return ((Track) o).id == id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Track{id=%d, title='%s', user=%s}", id, title, user);
    }
}