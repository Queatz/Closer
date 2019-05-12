package closer.vlllage.com.closer.handler.helpers;

import android.location.Location;

import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;

import static android.location.Location.distanceBetween;

public class DistanceHandler extends PoolMember {
    public boolean isUserNearGroup(Group group) {
        if (!group.isPhysical()) {
            return false;
        }

        float[] results = new float[1];
        Location location = $(LocationHandler.class).getLastKnownLocation();

        if (location == null) {
            return false;
        }

        distanceBetween(group.getLatitude(), group.getLongitude(), location.getLatitude(), location.getLongitude(), results);

        return results[0] < 402;
    }
}
