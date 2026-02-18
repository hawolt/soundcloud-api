package com.hawolt.ffmpeg;

import com.hawolt.logger.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioConverter {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public AudioConverter() throws IOException {
        this.ffmpeg = new FFmpeg(FFmpegSupplier.getFFmpegPath());
        this.ffprobe = new FFprobe(FFmpegSupplier.getFFprobePath());
    }

    public byte[] convertToMP3(byte[] bytes) throws IOException {
        Path in = null;
        Path out = null;

        try {
            in = Files.createTempFile("audio_input_", ".dat");
            Files.write(in, bytes);

            FFmpegProbeResult probeResult = ffprobe.probe(in.toString());
            String formatName = probeResult.getFormat().format_name;

            Logger.debug("Detected audio format: {}", formatName);

            if (isMP3(formatName)) {
                Logger.debug("Input is already MP3, skipping conversion");
                return bytes;
            }

            out = Files.createTempFile("audio_output_", ".mp3");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(in.toString())
                    .overrideOutputFiles(true)
                    .addOutput(out.toString())
                    .setFormat("mp3")
                    .setAudioCodec("libmp3lame")
                    .setAudioBitRate(320000)
                    .setAudioChannels(2)
                    .setAudioSampleRate(44100)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            byte[] converted = Files.readAllBytes(out);
            Logger.debug("Conversion complete: {} bytes -> {} bytes", bytes.length, converted.length);

            return converted;

        } finally {
            deleteSilently(in);
            deleteSilently(out);
        }
    }

    private boolean isMP3(String formatName) {
        if (formatName == null) return false;
        return formatName.toLowerCase().contains("mp3");
    }

    private void deleteSilently(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {

        }
    }
}