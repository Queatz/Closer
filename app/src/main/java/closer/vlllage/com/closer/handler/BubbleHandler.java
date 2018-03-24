package closer.vlllage.com.closer.handler;

import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import closer.vlllage.com.closer.handler.bubble.BubbleMapLayer;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuView;
import closer.vlllage.com.closer.handler.bubble.MapBubbleSuggestionView;
import closer.vlllage.com.closer.handler.bubble.MapBubbleView;
import closer.vlllage.com.closer.pool.PoolMember;

public class BubbleHandler extends PoolMember {

    private final BubbleMapLayer bubbleMapLayer = new BubbleMapLayer();

    public void attach(GoogleMap map, ViewGroup bubbleMapLayerLayout,
                       MapBubbleView.OnMapBubbleClickListener onClickListener,
                       MapBubbleMenuView.OnMapBubbleMenuItemClickListener onMenuItemClickListener,
                       MapBubbleSuggestionView.MapBubbleSuggestionClickListener onMapBubbleSuggestionClickListener) {
        bubbleMapLayer.attach(map, bubbleMapLayerLayout, onClickListener,
                onMenuItemClickListener, onMapBubbleSuggestionClickListener);
    }

    public void add(final MapBubble mapBubble) {
        bubbleMapLayer.add(mapBubble);
    }

    public void replace(List<MapBubble> mapBubbles) {
        bubbleMapLayer.replace(mapBubbles);
    }

    public void update() {
        bubbleMapLayer.update();
    }

    public void update(MapBubble mapBubble) {
        bubbleMapLayer.update(mapBubble);
    }

    public void move(MapBubble mapBubble, LatLng latLng) {
        bubbleMapLayer.move(mapBubble, latLng);
    }

    public void updateDetails(MapBubble mapBubble) {
        bubbleMapLayer.updateDetails(mapBubble);
    }

    public void remove(MapBubble mapBubble) {
        bubbleMapLayer.remove(mapBubble);
    }
}
