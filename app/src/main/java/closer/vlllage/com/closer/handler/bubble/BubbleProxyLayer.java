package closer.vlllage.com.closer.handler.bubble;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BubbleProxyLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private BubbleMapLayer bubbleMapLayer;

    public BubbleProxyLayer(BubbleMapLayer bubbleMapLayer) {
        this.bubbleMapLayer = bubbleMapLayer;
    }

    public void recalculate() {
        // Remove old proxies

        Set<MapBubble> toRemove = new HashSet<>();
        for (MapBubble mapBubble : mapBubbles) {
            if (BubbleType.PROXY.equals(mapBubble.getType())) {
                toRemove.add(mapBubble);
            }
        }

        for (MapBubble mapBubble : toRemove) {
            mapBubbles.remove(mapBubble);
        }

        // Add new proxies

        MapBubble proxyMapBubble = new MapBubble(new LatLng(0, 0), BubbleType.PROXY);
        proxyMapBubble.setPinned(false);
        proxyMapBubble.setOnTop(false);
        float lat = 0, lng = 0;
        int num = 0;
        for (MapBubble mapBubble : mapBubbles) {
            if (BubbleType.STATUS.equals(mapBubble.getType())) {
                lat += mapBubble.getLatLng().latitude;
                lng += mapBubble.getLatLng().longitude;
                num++;
            }
        }
        proxyMapBubble.setLatLng(new LatLng(
                lat / num,
                lng / num
        ));
// TODO       mapBubbles.add(proxyMapBubble);

        // Add bubbles
        for (MapBubble mapBubble : mapBubbles) {
            if (!bubbleMapLayer.getMapBubbles().contains(mapBubble)) {
                bubbleMapLayer.add(mapBubble);
            }
        }

        // Remove bubbles
        toRemove = new HashSet<>();

        for (MapBubble mapBubble : bubbleMapLayer.getMapBubbles()) {
            if (!mapBubbles.contains(mapBubble)) {
                toRemove.add(mapBubble);
            }
        }

        for (MapBubble mapBubble : toRemove) {
            bubbleMapLayer.remove(mapBubble);
        }

        // Move bubbles
        for (MapBubble mapBubble : bubbleMapLayer.getMapBubbles()) {
            if (mapBubble.getRawLatLng() != null) {
                bubbleMapLayer.move(mapBubble, mapBubble.getRawLatLng());
                mapBubble.setRawLatLng(null);
            }
        }
    }

    public void add(final MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
        recalculate();
    }

    public void replace(List<MapBubble> mapBubbles) {
        Map<String, MapBubble> byPhone = new HashMap<>();

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getPhone() != null) {
                byPhone.put(mapBubble.getPhone(), mapBubble);
            }
        }

        Set<String> updatedBubbles = new HashSet<>();
        Set<MapBubble> toRemoveBubbles = new HashSet<>();

        for (MapBubble mapBubble : this.mapBubbles) {
            if (mapBubble.isPinned()) {
                continue;
            }

            if (mapBubble.getPhone() == null || !byPhone.containsKey(mapBubble.getPhone())) {
                toRemoveBubbles.add(mapBubble);
            } else {
                mapBubble.updateFrom(byPhone.get(mapBubble.getPhone()));
                mapBubble.setRawLatLng(byPhone.get(mapBubble.getPhone()).getLatLng());
                bubbleMapLayer.updateDetails(mapBubble);
                updatedBubbles.add(mapBubble.getPhone());
            }
        }

        for (MapBubble mapBubble : toRemoveBubbles) {
            this.mapBubbles.remove(mapBubble);
        }

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getPhone() != null && !updatedBubbles.contains(mapBubble.getPhone())) {
                this.mapBubbles.add(mapBubble);
            }
        }
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
            mapBubbles.remove(mapBubble);
        }

        recalculate();
        return true;
    }

    public void move(MapBubble mapBubble, LatLng latLng) {
        mapBubble.setRawLatLng(latLng);
        recalculate();
    }
}
