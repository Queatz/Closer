package closer.vlllage.com.closer.handler.bubble;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.handler.map.ClusterMap;

public class BubbleProxyLayer {

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private BubbleMapLayer bubbleMapLayer;

    public BubbleProxyLayer(BubbleMapLayer bubbleMapLayer) {
        this.bubbleMapLayer = bubbleMapLayer;
    }

    public void recalculate() {
        // Proxies

        Set<MapBubble> preCalculationProxyBubbles = new HashSet<>();

        Set<MapBubble> postCalculationProxyBubbles = new HashSet<>();

        mergeBubbles(mapBubbles, preCalculationProxyBubbles, postCalculationProxyBubbles);

        mapBubbles.removeAll(preCalculationProxyBubbles);
        mapBubbles.addAll(postCalculationProxyBubbles);

        // Add bubbles
        for (MapBubble mapBubble : mapBubbles) {
            if (!bubbleMapLayer.getMapBubbles().contains(mapBubble)) {
                bubbleMapLayer.add(mapBubble);
            }
        }

        // Remove bubbles
        preCalculationProxyBubbles = new HashSet<>();

        for (MapBubble mapBubble : bubbleMapLayer.getMapBubbles()) {
            if (!mapBubbles.contains(mapBubble)) {
                preCalculationProxyBubbles.add(mapBubble);
            }
        }

        for (MapBubble mapBubble : preCalculationProxyBubbles) {
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

    private void mergeBubbles(Set<MapBubble> mapBubbles, Set<MapBubble> preProxyBubbles, Set<MapBubble> postProxyBubbles) {
        ClusterMap clusterMap = new ClusterMap(.05);

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getType() == BubbleType.PROXY) {
                preProxyBubbles.add(mapBubble);
            } else if (mapBubble.getType() == BubbleType.STATUS || mapBubble.getType() == BubbleType.EVENT || mapBubble.getType() == BubbleType.PHYSICAL_GROUP) {
                clusterMap.add(mapBubble);
            }
        }

        List<Set<MapBubble>> clusters = clusterMap.generateClusters();

        for (Set<MapBubble> cluster : clusters) {
            MapBubble proxyMapBubble = new MapBubble(new LatLng(0, 0), BubbleType.PROXY);
            proxyMapBubble.setPinned(false);
            proxyMapBubble.setOnTop(false);
            proxyMapBubble.proxies(cluster);
            float lat = 0, lng = 0;
            int num = 0;
            for (MapBubble mapBubble : cluster) {
                lat += mapBubble.getLatLng().latitude;
                lng += mapBubble.getLatLng().longitude;
                num++;
            }
            proxyMapBubble.setLatLng(new LatLng(
                    lat / num,
                    lng / num
            ));

            postProxyBubbles.add(proxyMapBubble);
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
