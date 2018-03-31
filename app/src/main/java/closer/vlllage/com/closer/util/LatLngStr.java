package closer.vlllage.com.closer.util;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LatLngStr {

    public static String from(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }

    public static LatLng to(String latLng) {
        String[] parts = latLng.split(",");
        return new LatLng(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
    }

    public static LatLng to(List<Double> latLng) {
        if (latLng == null) {
            return null;
        }

        return new LatLng(latLng.get(0), latLng.get(1));
    }
}
