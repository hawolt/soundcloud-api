package com.hawolt.data.media.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Tags {

    private static final Pattern QUOTED_TAG_PATTERN = Pattern.compile("\"([^\"]*)\"");

    private final List<String> list;

    public Tags(String genre, String tagline) {
        List<String> tags = new ArrayList<>();

        if (tagline != null && !tagline.isEmpty()) {

            Matcher matcher = QUOTED_TAG_PATTERN.matcher(tagline);
            while (matcher.find()) {
                String quoted = matcher.group(1).trim();
                if (!quoted.isEmpty()) {
                    tags.add(quoted);
                }
            }

            String remainder = QUOTED_TAG_PATTERN.matcher(tagline).replaceAll("").trim();
            if (!remainder.isEmpty()) {
                for (String tag : remainder.split("\\s+")) {
                    if (!tag.isEmpty()) {
                        tags.add(tag);
                    }
                }
            }
        }

        if (genre != null && !genre.isEmpty()) {
            tags.add(genre);
        }

        this.list = Collections.unmodifiableList(tags);
    }

    public boolean contains(String tag) {
        return list.contains(tag);
    }

    public boolean anyContains(String t) {
        String lower = t.toLowerCase();
        return list.stream().anyMatch(tag -> tag.toLowerCase().contains(lower));
    }

    public boolean anyMatch(String expression) {
        return list.stream().anyMatch(tag -> tag.matches(expression));
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    @Override
    public String toString() {
        return list.stream()
                .filter(tag -> !tag.isEmpty())
                .map(tag -> "#" + tag)
                .collect(Collectors.joining(", "));
    }

    public List<String> getList() {
        return list;
    }
}