package closer.vlllage.com.closer.handler.map;

import android.app.Fragment;
import android.content.Intent;

import closer.vlllage.com.closer.pool.PoolMember;

public class MapViewHandler extends PoolMember {

    private MapSlideFragment mapSlideFragment;

    public Fragment getMapFragment() {
        if (mapSlideFragment == null) {
            mapSlideFragment = new MapSlideFragment();
        }
        return mapSlideFragment;
    }

    public boolean onBackPressed() {
        return mapSlideFragment.onBackPressed();
    }

    public void handleIntent(Intent intent) {
        mapSlideFragment.handleIntent(intent);
    }
}
