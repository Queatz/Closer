package closer.vlllage.com.closer.handler;

import android.content.Intent;

import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Suggestion;

import static closer.vlllage.com.closer.MapsActivity.EXTRA_LAT_LNG;
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
}
