package closer.vlllage.com.closer.handler;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;

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

    public void update(MapBubble mapBubble) {
        bubbleMapLayer.update(mapBubble);
    }

    public void updateDetails(MapBubble mapBubble) {
        bubbleMapLayer.updateDetails(mapBubble);
    }

    public void remove(MapBubble mapBubble) {
        bubbleMapLayer.remove(mapBubble);
    }
}
