package closer.vlllage.com.closer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.github.queatz.slidescreen.SlideScreen;
import com.github.queatz.slidescreen.SlideScreenAdapter;

import closer.vlllage.com.closer.handler.map.MapViewHandler;
import closer.vlllage.com.closer.pool.PoolActivity;

public class MapsActivity extends PoolActivity {

    public static final String EXTRA_LAT_LNG = "latLng";
    public static final String EXTRA_SUGGESTION = "suggestion";
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_MESSAGE = "message";

    private SlideScreen slideScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slidescreen);
        slideScreen = findViewById(R.id.slideScreen);
        slideScreen.setAdapter(new SlideScreenAdapter() {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getSlide(int position) {
                return position == 0 ? $(MapViewHandler.class).getMapFragment() : new Fragment();
            }

            @Override
            public FragmentManager getFragmentManager() {
                return MapsActivity.this.getFragmentManager();
            }
        });

        if (getIntent() != null) {
            onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        $(MapViewHandler.class).handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        $(MapViewHandler.class).onBackPressed(result -> {
            if (result) {
                return;
            }

            if (!slideScreen.isExpose()) {
                slideScreen.expose(true);
                return;
            }

            super.onBackPressed();
        });
    }
}
