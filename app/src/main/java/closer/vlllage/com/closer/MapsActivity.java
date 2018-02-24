package closer.vlllage.com.closer;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.BubbleHandler;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.IntentHandler;
import closer.vlllage.com.closer.handler.LocationHandler;
import closer.vlllage.com.closer.handler.MapHandler;
import closer.vlllage.com.closer.handler.MyBubbleHandler;
import closer.vlllage.com.closer.handler.ReplyLayoutHandler;
import closer.vlllage.com.closer.handler.StatusLayoutHandler;
import closer.vlllage.com.closer.pool.PoolActivity;

public class MapsActivity extends PoolActivity {

    public static final String EXTRA_LAT_LNG = "latLng";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STATUS = "status";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        $(MapHandler.class).setOnMapReadyListener(map -> $(BubbleHandler.class).attach(map, findViewById(R.id.bubbleMapLayer), $(ReplyLayoutHandler.class)::replyTo));
        $(MapHandler.class).setOnMapChangedListener($(BubbleHandler.class)::update);
        $(MapHandler.class).setOnMapClickedListener(latLng -> $(ReplyLayoutHandler.class).showReplyLayout(false));
        $(MapHandler.class).setOnMapIdleListener(latLng -> $(DisposableHandler.class).add($(ApiHandler.class).load(latLng).subscribe(mapBubbles -> $(BubbleHandler.class).replace(mapBubbles))));
        $(MapHandler.class).attach((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        $(MyBubbleHandler.class).start();
        $(ReplyLayoutHandler.class).attach(findViewById(R.id.replyLayout));
        $(StatusLayoutHandler.class).attach(findViewById(R.id.myStatusLayout));
        $(LocationHandler.class).getCurrentLocation(location -> $(AccountHandler.class).updateGeo(new LatLng(location.getLatitude(), location.getLongitude())));

        if (getIntent() != null) {
            $(IntentHandler.class).onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        $(IntentHandler.class).onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if ($(ReplyLayoutHandler.class).isVisible()) {
            $(ReplyLayoutHandler.class).showReplyLayout(false);
            return;
        }

        super.onBackPressed();
    }
}
