package closer.vlllage.com.closer.handler;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

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
    }

    public void add(final MapBubble mapBubble) {
        bubbleMapLayer.add(mapBubble);

        if (Math.random() < 0.1) {
            bubbleMapLayerLayout.postDelayed(() -> {
                bubbleMapLayer.move(mapBubble, new LatLng(mapBubble.getLatLng().latitude + 0.003, mapBubble.getLatLng().longitude - 0.012));
            }, 987);
        }
    }

    public void replace(List<MapBubble> mapBubbles) {
        bubbleMapLayer.clear();

        for (MapBubble mapBubble : mapBubbles) {
            add(mapBubble);
        }
    }

    public void update() {
        bubbleMapLayer.update();
    }

}
