package closer.vlllage.com.closer.handler.map;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Suggestion;

import static closer.vlllage.com.closer.GroupActivity.EXTRA_GROUP_ID;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_EVENT_ID;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_NAME;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_PHONE;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_SCREEN;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_STATUS;
import static closer.vlllage.com.closer.MapsActivity.EXTRA_SUGGESTION;

public class MapActivityHandler extends PoolMember {

    public void showSuggestionOnMap(Suggestion suggestion) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_LAT_LNG, new float[] {
                suggestion.getLatitude().floatValue(),
                suggestion.getLongitude().floatValue()
        });
        intent.putExtra(EXTRA_SUGGESTION, suggestion.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.shared_location) : suggestion.getName());

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public void showEventOnMap(Event event) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_LAT_LNG, new float[] {
                event.getLatitude().floatValue(),
                event.getLongitude().floatValue()
        });
        intent.putExtra(EXTRA_EVENT_ID, event.getId());

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public void showGroupOnMap(Group group) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_LAT_LNG, new float[] {
                group.getLatitude().floatValue(),
                group.getLongitude().floatValue()
        });
        intent.putExtra(EXTRA_GROUP_ID, group.getId());

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public void replyToPhone(String phoneId, String name, String status, @Nullable LatLng latLng) {
        Intent intent = new Intent($(ActivityHandler.class).getActivity(), MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        if (latLng != null) {
            intent.putExtra(EXTRA_LAT_LNG, new float[]{
                    (float) latLng.latitude,
                    (float) latLng.longitude
            });
        }

        name = name.isEmpty() ?
                $(ResourcesHandler.class).getResources().getString(R.string.app_name) :
                name;

        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_PHONE, phoneId);

        $(ActivityHandler.class).getActivity().startActivity(intent);
    }

    public void goToScreen(String screenName) { Intent intent = new Intent($(ActivityHandler.class).getActivity(), MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(EXTRA_SCREEN, screenName);
        $(ActivityHandler.class).getActivity().startActivity(intent);

    }
}
