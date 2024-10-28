package com.hawolt.data.media.hydratable.impl.user;

import com.hawolt.data.media.ObjectCallback;
import com.hawolt.data.media.hydratable.HydratableInterface;



public class UserManager implements HydratableInterface<User> {
    private final ObjectCallback<User> callback;

    public UserManager(ObjectCallback<User> callback) {
        this.callback = callback;
    }

    @Override
    public void accept(String link, User hydratable, String... args) {
        callback.ping(link, hydratable, args);
    }
}
