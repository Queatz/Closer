package closer.vlllage.com.closer.handler.bubble;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BubbleProxyLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private BubbleMapLayer bubbleMapLayer;

    public BubbleProxyLayer(BubbleMapLayer bubbleMapLayer) {
        this.bubbleMapLayer = bubbleMapLayer;
    }

    public void recalculate() {
        // Move bubbles in and out of proxies
        // Add remove proxy bubbles
    }

    public void add(final MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
        recalculate();
    }

    public void replace(List<MapBubble> mapBubbles) {
        this.mapBubbles.clear();
        this.mapBubbles.addAll(mapBubbles);
        recalculate();
    }

    public void remove(MapBubble mapBubble) {
        mapBubbles.remove(mapBubble);
        recalculate();
    }

    public boolean remove(BubbleMapLayer.RemoveCallback callback) {
        Set<MapBubble> toRemove = new HashSet<>();
        for (MapBubble mapBubble : mapBubbles) {
            if (callback.apply(mapBubble)) {
                toRemove.add(mapBubble);
            }
        }

        if (toRemove.isEmpty()) {
            return false;
        }

        for (MapBubble mapBubble : toRemove) {
            remove(mapBubble);
        }

        recalculate();
        return true;
    }

    public void move(MapBubble mapBubble, LatLng latLng) {
        mapBubble.setRawLatLng(latLng);
        recalculate();
    }
}
