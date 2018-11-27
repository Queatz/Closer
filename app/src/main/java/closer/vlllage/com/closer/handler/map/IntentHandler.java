package closer.vlllage.com.closer.handler.map;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Suggestion;

import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_EVENT_ID;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_NAME;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_PHONE;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_STATUS;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_SUGGESTION;

public class IntentHandler extends PoolMember {

    public void onNewIntent(Intent intent, MapViewHandler.OnRequestMapOnScreenListener onRequestMapOnScreenListener) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (intent.hasExtra(EXTRA_STATUS)) {
                float[] latLng = intent.getFloatArrayExtra(EXTRA_LAT_LNG);
                $(ReplyLayoutHandler.class).replyTo(new MapBubble(
                        intent.hasExtra(EXTRA_LAT_LNG) ? new LatLng(latLng[0], latLng[1]) : null,
                        intent.hasExtra(EXTRA_NAME) ? intent.getStringExtra(EXTRA_NAME) : "",
                        intent.getStringExtra(EXTRA_STATUS)
                ).setPhone(intent.getStringExtra(EXTRA_PHONE)));
                onRequestMapOnScreenListener.onRequestMapOnScreen();
            } else if (intent.hasExtra(EXTRA_EVENT_ID) || intent.hasExtra(EXTRA_GROUP_ID)) {
                float[] latLngFloats = intent.getFloatArrayExtra(EXTRA_LAT_LNG);
                LatLng latLng = new LatLng(latLngFloats[0], latLngFloats[1]);
                $(MapHandler.class).centerMap(latLng);
                onRequestMapOnScreenListener.onRequestMapOnScreen();
            } else if (intent.hasExtra(EXTRA_SUGGESTION)) {
                float[] latLngFloats = intent.getFloatArrayExtra(EXTRA_LAT_LNG);
                LatLng latLng = new LatLng(latLngFloats[0], latLngFloats[1]);

                Suggestion suggestion = new Suggestion();
                suggestion.setLatitude(latLng.latitude);
                suggestion.setLongitude(latLng.longitude);
                suggestion.setName(intent.getStringExtra(EXTRA_SUGGESTION));

                $(SuggestionHandler.class).clearSuggestions();
                $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));

                $(BubbleHandler.class).add($(SuggestionHandler.class).suggestionBubbleFrom(suggestion));
                $(MapHandler.class).centerMap(latLng);
                onRequestMapOnScreenListener.onRequestMapOnScreen();
            }
        }
    }
}
