package io.bclub.util;

import android.location.Location;

public class LocationCache {

    Location location;
    long timestamp;

    long validityMilliseconds = 0L;

    public LocationCache(long validityMilliseconds) {
        this.validityMilliseconds = validityMilliseconds;
    }

    public Location get() {
        long diff = System.currentTimeMillis() - timestamp;

        if (diff > validityMilliseconds) {
            return null;
        }

        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.timestamp = System.currentTimeMillis();
    }
}
