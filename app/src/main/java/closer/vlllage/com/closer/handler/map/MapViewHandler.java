package closer.vlllage.com.closer.handler.map;

import android.app.Fragment;
import android.content.Intent;

import closer.vlllage.com.closer.pool.PoolMember;

public class MapViewHandler extends PoolMember {

    private MapSlideFragment mapSlideFragment;
    private OnRequestMapOnScreenListener onRequestMapOnScreenListener;

    public Fragment getMapFragment() {
        if (mapSlideFragment == null) {
            mapSlideFragment = new MapSlideFragment();
        }
        return mapSlideFragment;
    }

    public void onBackPressed(OnBackPressedCallback callback) {
        mapSlideFragment.post(() -> callback.onBackPressedResult(mapSlideFragment.onBackPressed()));
    }

    public void handleIntent(Intent intent) {
        mapSlideFragment.post(() -> mapSlideFragment.handleIntent(intent, onRequestMapOnScreenListener));
    }

    public MapViewHandler setOnRequestMapOnScreenListener(OnRequestMapOnScreenListener onRequestMapOnScreenListener) {
        this.onRequestMapOnScreenListener = onRequestMapOnScreenListener;
        return this;
    }

    public interface OnBackPressedCallback {
        void onBackPressedResult(boolean result);
    }

    public interface OnRequestMapOnScreenListener {
        void onRequestMapOnScreen();
    }
}
