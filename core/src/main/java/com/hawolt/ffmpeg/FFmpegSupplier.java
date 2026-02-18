package com.hawolt.ffmpeg;

import com.hawolt.logger.Logger;

import java.io.File;

public class FFmpegSupplier {

    private static String ffmpegPath;
    private static String ffprobePath;

    public static synchronized String getFFmpegPath() {
        if (ffmpegPath == null) ffmpegPath = resolveExecutable("ffmpeg");
        return ffmpegPath;
    }

    public static synchronized String getFFprobePath() {
        if (ffprobePath == null) ffprobePath = resolveExecutable("ffprobe");
        return ffprobePath;
    }

    private static String resolveExecutable(String name) {
        try {
            try {
                Class<?> loaderClass = Class.forName("org.bytedeco.javacpp.Loader");
                Object object = loaderClass.getMethod("load", Class.class).invoke(null, Class.forName("org.bytedeco.ffmpeg." + name));
                String path = object.toString();
                if (path != null && new File(path).exists()) {
                    Logger.debug("Resolved {} from Bytedeco: {}", name, path);
                    return path;
                }
            } catch (ClassNotFoundException e) {

            }
        } catch (Exception e) {
            Logger.debug("Bytedeco {} not found, falling back to system PATH", name);
        }
        return name;
    }
}