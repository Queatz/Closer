package closer.vlllage.com.closer.handler;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.pool.PoolMember;

public class OutboundHandler extends PoolMember {
    public void openDirections(LatLng latLng) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latLng.latitude + "," + latLng.longitude));
        $(ActivityHandler.class).getActivity().startActivity(intent);
    }
}
