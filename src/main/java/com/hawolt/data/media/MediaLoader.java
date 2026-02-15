package com.hawolt.data.media;

import com.hawolt.data.VirtualClient;
import com.hawolt.data.media.hydratable.Hydratable;
import com.hawolt.ionhttp.IonClient;
import com.hawolt.ionhttp.request.IonRequest;
import com.hawolt.ionhttp.request.IonResponse;
import com.hawolt.logger.Logger;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MediaLoader implements Callable<IonResponse> {
    private static final int MAX_DELAY_SECONDS = 30;
    private static final int MAX_RETRIES = 10;

    private static final Pattern pattern = Pattern.compile("client_id=([^&]+)");
    private String resource;

    public MediaLoader(String resource) {
        this.resource = resource;
    }

    private int duration = 1;

    @Override
    public IonResponse call() throws Exception {
        IonResponse response;
        int code;
        int retries = 0;
        do {
            if (retries++ > MAX_RETRIES) {
                throw new IOException("Max retries exceeded for resource: " + resource);
            }
            IonRequest.SimpleBuilder builder = IonRequest.on(resource);
            builder.addHeader("Host", builder.hostname);
            builder.addHeader("User-Agent", "SoundCloudAPI-V1.0.0-" + UUID.randomUUID().toString());
            IonRequest request = builder.get();
            response = IonClient.getDefault().execute(request);
            code = response.code();
            if (code == 401) {
                resource = getNewResourceLocation();
            } else if (code == 429 || code == 403 || code == 203) {
                duration = Math.min(duration * 3, MAX_DELAY_SECONDS);
                long delay = duration * 1000L;
                Logger.debug("Snooze {} for {} on {}", delay, code, resource);
                Hydratable.snooze(delay);
            }
        } while (code == 429 || code == 403 || code == 401 || code == 203);
        return response;
    }

    private String getNewResourceLocation() throws Exception {
        String clientId = VirtualClient.getID(true);
        Matcher matcher = pattern.matcher(resource);
        if (matcher.find()) {
            return matcher.replaceAll("client_id=" + clientId);
        } else {
            throw new Exception("Unable to adjust client_id");
        }
    }
}