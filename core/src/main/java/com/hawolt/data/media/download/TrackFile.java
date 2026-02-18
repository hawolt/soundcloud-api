package com.hawolt.data.media.download;

import com.hawolt.SoundcloudInternal;
import com.hawolt.data.media.download.impl.TrackFragment;
import com.hawolt.data.media.hydratable.impl.Track;
import com.hawolt.data.media.track.EXTM3U;
import com.hawolt.data.media.track.MP3;
import com.hawolt.ffmpeg.AudioConverter;
import com.hawolt.logger.Logger;
import org.checkerframework.checker.units.qual.A;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TrackFile implements IFile, FileCallback {

    private static volatile ExecutorService DEFAULT_EXECUTOR = Executors.newFixedThreadPool(1);

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private static AudioConverter AUDIO_CONVERTER;

    static {
        try {
            AUDIO_CONVERTER = new AudioConverter();
        } catch (IOException e) {
            Logger.error("""
                    You can add this dependency to have FFmpeg work as plug-and-play
                    
                            <dependency>
                                <groupId>org.bytedeco</groupId>
                                <artifactId>ffmpeg-platform</artifactId>
                                <version>7.1.1-1.5.12</version>
                                <scope>compile</scope>
                            </dependency>
                    """);
            Logger.error("Failed to initialize AudioConverter, track data will be served raw.");
        }
    }

    public static ExecutorService getDefaultExecutor() {
        return DEFAULT_EXECUTOR;
    }

    public static void setDefaultExecutor(ExecutorService executor) {
        DEFAULT_EXECUTOR = executor;
    }


    private final Map<Integer, TrackFragment> fragments = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final DownloadCallback callback;
    private final ExecutorService service;
    private final int total;
    private final MP3 mp3;

    private TrackFile(DownloadCallback callback, MP3 mp3, ExecutorService service) {
        this.callback = callback;
        this.service = service;
        this.mp3 = mp3;

        EXTM3U extm3u = mp3.getEXTM3U();
        if (extm3u == null) {
            this.total = 0;
            this.callback.onFailure(mp3.getTrack(), -1);
            return;
        }

        List<String> urls = extm3u.getFragmentList();
        this.total = urls.size();

        for (int i = 0; i < total; i++) {
            fragments.put(i, new TrackFragment(this, i, urls.get(i)));
        }

        for (int i = 0; i < total; i++) {
            load(fragments.get(i));
        }
    }

    public static TrackFile create(DownloadCallback callback, MP3 mp3) {
        return new TrackFile(callback, mp3, DEFAULT_EXECUTOR);
    }

    public static TrackFile create(DownloadCallback callback, MP3 mp3, ExecutorService service) {
        return new TrackFile(callback, mp3, service);
    }

    private void load(TrackFragment fragment) {
        if (service != null) {
            service.execute(fragment);
        } else {
            fragment.run();
        }
    }

    @Override
    public byte[] bytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < total; i++) {
            TrackFragment fragment = fragments.get(i);
            if (fragment == null) {
                Logger.error("Missing fragment {} during assembly", i);
                continue;
            }
            try {
                out.write(fragment.bytes());
            } catch (IOException e) {
                Logger.error("Failed to write fragment {} during assembly: {}", i, e.getMessage());
            }
        }
        return out.toByteArray();
    }

    @Override
    public void onAssembly(int index, IFile file) {
        Track track = mp3.getTrack();

        Logger.debug("Downloaded fragment [{}/{}] of {}", index, total - 1, track.getPermalink());

        if (counter.incrementAndGet() == total) {
            Logger.debug("Assembled track {}", track.getPermalink());

            byte[] bytes = bytes();

            if (AUDIO_CONVERTER != null) {
                try {
                    bytes = AUDIO_CONVERTER.convertToMP3(bytes());
                } catch (IOException e) {
                    Logger.error("Failed to convert to MP3, continue with original content");
                }
            }

            if (SoundcloudInternal.writeID3Tag) {
                ID3TagWriter writer = new ID3TagWriter()
                        .title(track.getTitle())
                        .year(String.valueOf(Instant.ofEpochMilli(track.getCreatedAt()).atZone(ZoneId.of("UTC")).getYear()))
                        .artist(track.getUser() != null ? track.getUser().getPermalink() : null)
                        .genre(track.getGenre())
                        .comment(track.getLink());

                Logger.debug("Loading artwork for {}", track.getPermalink());

                try {
                    byte[] artwork = track.loadArtwork();
                    writer.artwork(artwork, "image/jpeg");
                } catch (IOException e) {
                    Logger.debug("Failed to load artwork for {}", track.getPermalink());
                }

                try {
                    callback.onCompletion(track, writer.apply(bytes));
                } catch (IOException e) {
                    Logger.debug("Failed to write ID3Tag for {}", track.getPermalink());
                    callback.onCompletion(track, bytes);
                }
            } else {
                callback.onCompletion(track, bytes);
            }
        }
    }

    @Override
    public void onFailure(int index, int attempt, String url) {
        Logger.error("Failed to download fragment {}:{} (attempt {})", index, url, attempt);

        if (attempt > MAX_RETRY_ATTEMPTS) {
            Logger.debug("Exceeded max retries for track {}", mp3.getTrack().getPermalink());
            callback.onFailure(mp3.getTrack(), index);
        } else {
            TrackFragment fragment = fragments.get(index);
            if (fragment != null) {
                load(fragment);
            } else {
                Logger.error("Cannot retry missing fragment {}", index);
                callback.onFailure(mp3.getTrack(), index);
            }
        }
    }

    public int getTotal() {
        return total;
    }

    public int getCompletedFragments() {
        return counter.get();
    }
}