package com.hawolt.data.media.download;

import com.hawolt.data.media.download.impl.TrackFragment;
import com.hawolt.data.media.track.EXTM3U;
import com.hawolt.data.media.track.MP3;
import com.hawolt.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class TrackFile implements IFile, FileCallback {
    private static ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private final AtomicInteger fragments = new AtomicInteger(0);
    private final Map<Integer, TrackFragment> map = new HashMap<>();
    private DownloadCallback callback;
    private ExecutorService service;
    private MP3 mp3;

    public TrackFile(DownloadCallback callback, MP3 mp3) {
        this(callback, mp3, EXECUTOR_SERVICE);
    }

    private TrackFile(DownloadCallback callback, MP3 mp3, ExecutorService service) {
        this.service = service;
        this.callback = callback;
        this.mp3 = mp3;
        EXTM3U extm3U = mp3.getEXTM3U();
        if (extm3U == null) {
            this.callback.onFailure(mp3.getTrack(), -1);
            return;
        }
        List<String> list = extm3U.getFragmentList();
        for (int i = 0; i < list.size(); i++) {
            map.put(i, new TrackFragment(this, i, list.get(i)));
        }
        for (int i = 0; i < list.size(); i++) {
            TrackFragment fragment = map.get(i);
            if (service != null) {
                service.execute(fragment);
            } else {
                fragment.run();
            }
        }
    }

    public static TrackFile get(DownloadCallback callback, MP3 mp3, ExecutorService service) {
        return new TrackFile(callback, mp3, service);
    }

    public static TrackFile get(DownloadCallback callback, MP3 mp3) {
        return new TrackFile(callback, mp3);
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < map.size(); i++) {
            TrackFragment fragment = map.get(i);
            try {
                out.write(fragment.getBytes());
            } catch (IOException e) {
                Logger.error(e);
            }
        }
        return out.toByteArray();
    }

    @Override
    public void onAssembly(int index, IFile file) {
        Logger.debug("Downloaded fragment [{}/{}] of {}", index, map.size() - 1, mp3.getTrack().getPermalink());
        if (fragments.incrementAndGet() == map.size()) {
            Logger.debug("Assembled track {}", mp3.getTrack().getPermalink());
            final byte[] bytes = getBytes();
            callback.onCompletion(mp3.getTrack(), bytes);
        }
    }

    @Override
    public void onFailure(int index, int attempt, String url) {
        Logger.error("Failed to download fragment {}:{}", index, url);
        if (attempt > 3) {
            Logger.debug("Failed to download track {}", mp3.getTrack().getPermalink());
            callback.onFailure(mp3.getTrack(), index);
        } else {
            if (service != null) {
                service.execute(map.get(index));
            } else {
                map.get(index).run();
            }
        }
    }

    public static ExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }
}
