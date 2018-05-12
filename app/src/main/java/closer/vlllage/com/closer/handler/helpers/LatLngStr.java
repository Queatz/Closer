package closer.vlllage.com.closer.handler.helpers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.pool.PoolMember;

public class LatLngStr extends PoolMember {

    public String from(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }

    public LatLng to(String latLng) {
        String[] parts = latLng.split(",");
        return new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
    }

    public LatLng to(List<Double> latLng) {
        if (latLng == null) {
            return null;
        }

        return new LatLng(latLng.get(0), latLng.get(1));
    }
}
