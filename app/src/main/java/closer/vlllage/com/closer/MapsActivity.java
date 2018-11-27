package closer.vlllage.com.closer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import com.github.queatz.slidescreen.SlideScreen;
import com.github.queatz.slidescreen.SlideScreenAdapter;

import closer.vlllage.com.closer.handler.PersonalSlideFragment;
import closer.vlllage.com.closer.handler.map.MapViewHandler;
import closer.vlllage.com.closer.handler.settings.SettingsSlideFragment;
import closer.vlllage.com.closer.pool.PoolActivity;

public class MapsActivity extends PoolActivity {

    public static final String EXTRA_LAT_LNG = "latLng";
    public static final String EXTRA_SUGGESTION = "suggestion";
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_SCREEN = "screen";
    public static final String EXTRA_SCREEN_MAP = "screen.map";
    public static final String EXTRA_SCREEN_PERSONAL = "screen.personal";
    public static final String EXTRA_SCREEN_SETTINGS = "screen.settings";

    public static final int POSITION_SCREEN_PERSONAL = 0;
    public static final int POSITION_SCREEN_MAP = 1;
    public static final int POSITION_SCREEN_SETTINGS = 2;
    public static final int NUM_SCREENS = 3;

    private SlideScreen slideScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slidescreen);
        slideScreen = findViewById(R.id.slideScreen);
        slideScreen.setAdapter(new SlideScreenAdapter() {
            @Override
            public int getCount() {
                return NUM_SCREENS;
            }

            @Override
            public Fragment getSlide(int position) {
                switch (position) {
                    case POSITION_SCREEN_PERSONAL: return new PersonalSlideFragment();
                    case POSITION_SCREEN_MAP: return $(MapViewHandler.class).getMapFragment();
                    case POSITION_SCREEN_SETTINGS: return new SettingsSlideFragment();
                    default: throw new IndexOutOfBoundsException();
                }
            }

            @Override
            public FragmentManager getFragmentManager() {
                return MapsActivity.this.getFragmentManager();
            }
        });

        if (getIntent() != null) {
            onNewIntent(getIntent());
        }

        $(MapViewHandler.class).setOnRequestMapOnScreenListener(() -> {
            slideScreen.setSlide(POSITION_SCREEN_MAP);
        });

        slideScreen.setSlide(POSITION_SCREEN_MAP);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(EXTRA_SCREEN)) {
            switch (intent.getStringExtra(EXTRA_SCREEN)) {
                case EXTRA_SCREEN_PERSONAL:
                    slideScreen.setSlide(POSITION_SCREEN_PERSONAL);
                    return;
                case EXTRA_SCREEN_MAP:
                    slideScreen.setSlide(POSITION_SCREEN_MAP);
                    return;
                case EXTRA_SCREEN_SETTINGS:
                    slideScreen.setSlide(POSITION_SCREEN_SETTINGS);
                    return;
            }
        }

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
