package closer.vlllage.com.closer.handler;

import android.location.Location;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.bubble.BubbleMapLayer;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleView;
import closer.vlllage.com.closer.pool.PoolMember;

public class BubbleHandler extends PoolMember {

    private final BubbleMapLayer bubbleMapLayer = new BubbleMapLayer();
    private View bubbleMapLayerLayout;

    public void attach(GoogleMap map, ViewGroup bubbleMapLayerLayout, MapBubbleView.OnMapBubbleClickListener onClickListener) {
        this.bubbleMapLayerLayout = bubbleMapLayerLayout;
        bubbleMapLayer.attach(map, bubbleMapLayerLayout, onClickListener);
        pool(LocationHandler.class).getCurrentLocation(this::onLocationFound);
    }

    private void onLocationFound(Location location) {
        bubbleMapLayerLayout.postDelayed(() -> {
            MapBubble alfred = new MapBubble(new LatLng(location.getLatitude() - .01, location.getLongitude()), "Alfred", "Walking the doggo");
            bubbleMapLayer.add(alfred);
            bubbleMapLayer.add(new MapBubble(new LatLng(location.getLatitude() + .01, location.getLongitude() + .02), "Meghan", "Homework"));

            bubbleMapLayerLayout.postDelayed(() -> {
                bubbleMapLayer.move(alfred, new LatLng(alfred.getLatLng().latitude + 0.003, alfred.getLatLng().longitude - 0.012));
            }, 987);
        }, 1000);
    }

    public void update() {
        bubbleMapLayer.update();
    }
}
