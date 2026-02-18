package com.hawolt.data.media.download;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ID3TagWriter {

    public static final String TITLE = "TIT2";
    public static final String ARTIST = "TPE1";
    public static final String ALBUM = "TALB";
    public static final String GENRE = "TCON";
    public static final String YEAR = "TDRC";
    public static final String COMMENT = "COMM";
    public static final String URL_USER = "WXXX";

    private final Map<String, String> textFrames = new LinkedHashMap<>();
    private String commentText;
    private String userUrl;
    private String userUrlDescription;
    private byte[] artworkData;
    private String artworkMimeType;

    public ID3TagWriter title(String value) {
        if (value != null && !value.isEmpty()) textFrames.put(TITLE, value);
        return this;
    }

    public ID3TagWriter artist(String value) {
        if (value != null && !value.isEmpty()) textFrames.put(ARTIST, value);
        return this;
    }

    public ID3TagWriter album(String value) {
        if (value != null && !value.isEmpty()) textFrames.put(ALBUM, value);
        return this;
    }

    public ID3TagWriter genre(String value) {
        if (value != null && !value.isEmpty()) textFrames.put(GENRE, value);
        return this;
    }

    public ID3TagWriter year(String value) {
        if (value != null && !value.isEmpty()) textFrames.put(YEAR, value);
        return this;
    }

    public ID3TagWriter comment(String value) {
        if (value != null && !value.isEmpty()) this.commentText = value;
        return this;
    }

    public ID3TagWriter artwork(byte[] data, String mimeType) {
        if (data != null && data.length > 0) {
            this.artworkData = data;
            this.artworkMimeType = mimeType != null ? mimeType : "image/jpeg";
        }
        return this;
    }

    public ID3TagWriter url(String description, String url) {
        if (url != null && !url.isEmpty()) {
            this.userUrlDescription = description != null ? description : "";
            this.userUrl = url;
        }
        return this;
    }

    public byte[] apply(byte[] bytes) throws IOException {
        ByteArrayOutputStream tagBody = new ByteArrayOutputStream();

        for (Map.Entry<String, String> entry : textFrames.entrySet()) {
            writeTextFrame(tagBody, entry.getKey(), entry.getValue());
        }

        if (commentText != null) {
            writeCommentFrame(tagBody, commentText);
        }

        if (userUrl != null) {
            writeUserUrlFrame(tagBody, userUrlDescription, userUrl);
        }

        if (artworkData != null) {
            writeArtworkFrame(tagBody, artworkData, artworkMimeType);
        }

        byte[] tagBodyBytes = tagBody.toByteArray();

        ByteArrayOutputStream output = new ByteArrayOutputStream(10 + tagBodyBytes.length + bytes.length);

        output.write(new byte[]{'I', 'D', '3'});
        output.write(new byte[]{3, 0});
        output.write(0);
        writeSyncsafeInt(output, tagBodyBytes.length);

        output.write(tagBodyBytes);
        output.write(bytes);

        return output.toByteArray();
    }

    private void writeTextFrame(ByteArrayOutputStream out, String frameId, String text) throws IOException {
        byte[] idBytes = frameId.getBytes(StandardCharsets.US_ASCII);
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        int frameDataSize = 1 + textBytes.length;

        out.write(idBytes);
        writeInt(out, frameDataSize);
        out.write(new byte[]{0, 0});
        out.write(0x03);
        out.write(textBytes);
    }

    private void writeCommentFrame(ByteArrayOutputStream out, String comment) throws IOException {
        byte[] commentBytes = comment.getBytes(StandardCharsets.UTF_8);
        int frameDataSize = 1 + 3 + 1 + commentBytes.length;

        out.write("COMM".getBytes(StandardCharsets.US_ASCII));
        writeInt(out, frameDataSize);
        out.write(new byte[]{0, 0});
        out.write(0x03);
        out.write(new byte[]{'e', 'n', 'g'});
        out.write(0x00);
        out.write(commentBytes);
    }

    private void writeUserUrlFrame(ByteArrayOutputStream out, String description, String url) throws IOException {
        byte[] descBytes = description.getBytes(StandardCharsets.UTF_8);
        byte[] urlBytes = url.getBytes(StandardCharsets.ISO_8859_1);
        int frameDataSize = 1 + descBytes.length + 1 + urlBytes.length;

        out.write("WXXX".getBytes(StandardCharsets.US_ASCII));
        writeInt(out, frameDataSize);
        out.write(new byte[]{0, 0});
        out.write(0x03);
        out.write(descBytes);
        out.write(0x00);
        out.write(urlBytes);
    }

    private void writeArtworkFrame(ByteArrayOutputStream out, byte[] imageData, String mimeType) throws IOException {
        byte[] mimeBytes = mimeType.getBytes(StandardCharsets.US_ASCII);
        int frameDataSize = 1 + mimeBytes.length + 1 + 1 + 1 + imageData.length;

        out.write("APIC".getBytes(StandardCharsets.US_ASCII));
        writeInt(out, frameDataSize);
        out.write(new byte[]{0, 0});
        out.write(0x00);
        out.write(mimeBytes);
        out.write(0x00);
        out.write(0x03);
        out.write(0x00);
        out.write(imageData);
    }

    private void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >> 24) & 0xFF);
        out.write((value >> 16) & 0xFF);
        out.write((value >> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private void writeSyncsafeInt(ByteArrayOutputStream out, int value) {
        out.write((value >> 21) & 0x7F);
        out.write((value >> 14) & 0x7F);
        out.write((value >> 7) & 0x7F);
        out.write(value & 0x7F);
    }
}