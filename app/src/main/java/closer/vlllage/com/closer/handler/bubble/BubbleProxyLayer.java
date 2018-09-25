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

    public void add(final MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
        recalculate();
        bubbleMapLayer.add(mapBubble);
    }

    public void replace(List<MapBubble> mapBubbles) {
        this.mapBubbles.clear();
        this.mapBubbles.addAll(mapBubbles);
        recalculate();
    }

    public void remove(MapBubble mapBubble) {
        mapBubbles.remove(mapBubble);
        recalculate();
        bubbleMapLayer.remove(mapBubble);
    }

    public boolean remove(BubbleMapLayer.RemoveCallback callback) {
        mapBubbles.size();
        recalculate();
        boolean success = bubbleMapLayer.remove(mapBubble -> {
            boolean remove = callback.apply(mapBubble);

            if (remove) {
                mapBubbles.remove(mapBubble);
            }

            return remove;
        });
        recalculate();
        return success;
    }

    public void move(MapBubble mapBubble, LatLng latLng) {
        mapBubble.setRawLatLng(latLng);
        recalculate();
        if (isInProxy(mapBubble)) {
            bubbleMapLayer.remove(mapBubble);
        } else {
            bubbleMapLayer.move(mapBubble, latLng);
        }
    }

    public void update() {
        recalculate();
        bubbleMapLayer.update();
    }

    public void update(MapBubble mapBubble) {
        bubbleMapLayer.update(mapBubble);
    }

    public void updateDetails(MapBubble mapBubble) {
        bubbleMapLayer.updateDetails(mapBubble);
    }

    private boolean isInProxy(MapBubble mapBubble) {
        return false;
    }

    private void recalculate() {

    }
}
