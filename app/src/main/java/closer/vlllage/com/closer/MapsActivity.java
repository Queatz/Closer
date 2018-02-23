package closer.vlllage.com.closer;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.SupportMapFragment;

import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.BubbleHandler;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.IntentHandler;
import closer.vlllage.com.closer.handler.MapHandler;
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

        pool(MapHandler.class).setOnMapReadyListener(map -> pool(BubbleHandler.class).attach(map, findViewById(R.id.bubbleMapLayer), pool(ReplyLayoutHandler.class)::replyTo));
        pool(MapHandler.class).setOnMapChangedListener(pool(BubbleHandler.class)::update);
        pool(MapHandler.class).setOnMapClickedListener(latLng -> pool(ReplyLayoutHandler.class).showReplyLayout(false));
        pool(MapHandler.class).setOnMapIdleListener(latLng -> pool(DisposableHandler.class).add(pool(ApiHandler.class).load(latLng).subscribe(mapBubbles -> pool(BubbleHandler.class).replace(mapBubbles))));
        pool(MapHandler.class).attach((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        pool(ReplyLayoutHandler.class).attach(findViewById(R.id.replyLayout));
        pool(StatusLayoutHandler.class).attach(findViewById(R.id.myStatusLayout));

        if (getIntent() != null) {
            pool(IntentHandler.class).onNewIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        pool(IntentHandler.class).onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (pool(ReplyLayoutHandler.class).isVisible()) {
            pool(ReplyLayoutHandler.class).showReplyLayout(false);
            return;
        }

        super.onBackPressed();
    }
}
