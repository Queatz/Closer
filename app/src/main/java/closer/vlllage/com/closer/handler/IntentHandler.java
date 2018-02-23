package closer.vlllage.com.closer.handler;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;

import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_NAME;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_STATUS;

public class IntentHandler extends PoolMember {
    public void onNewIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (!intent.hasExtra(EXTRA_LAT_LNG) || !intent.hasExtra(EXTRA_NAME) || !intent.hasExtra(EXTRA_STATUS)) {
                return;
            }

            float[] latLng = intent.getFloatArrayExtra(EXTRA_LAT_LNG);
            pool(ReplyLayoutHandler.class).replyTo(new MapBubble(
                    new LatLng(latLng[0], latLng[1]),
                    intent.getStringExtra(EXTRA_NAME),
                    intent.getStringExtra(EXTRA_STATUS)
            ));
        }
    }
}
