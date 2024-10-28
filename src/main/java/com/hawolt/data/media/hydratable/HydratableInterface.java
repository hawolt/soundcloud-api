package com.hawolt.data.media.hydratable;



public interface HydratableInterface<T extends Hydratable> {
    void accept(String link, T hydratable, String... args);
}
