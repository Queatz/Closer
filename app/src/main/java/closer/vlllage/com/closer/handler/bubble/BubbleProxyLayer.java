package closer.vlllage.com.closer.handler.bubble;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import closer.vlllage.com.closer.handler.map.ClusterMap;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Math.abs;

public class BubbleProxyLayer {

    private static final double MERGE_RESOLUTION = 4f;

    private final Set<MapBubble> mapBubbles = new HashSet<>();
    private BubbleMapLayer bubbleMapLayer;
    private MapViewportCallback mapViewportCallback;
    private double lastClusterSize;
    private Disposable mergeBubblesDisposable;

    public BubbleProxyLayer(BubbleMapLayer bubbleMapLayer, MapViewportCallback mapViewportCallback) {
        this.bubbleMapLayer = bubbleMapLayer;
        this.mapViewportCallback = mapViewportCallback;
    }

    public void recalculate() {
        lastClusterSize = getClusterSize();

        if (mergeBubblesDisposable != null) {
            mergeBubblesDisposable.dispose();
        }

        mergeBubblesDisposable = mergeBubbles(new HashSet<>(mapBubbles), lastClusterSize)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    // TODO check if proxies are the same and don't replace
                    mapBubbles.removeAll(result.preCalculationProxyBubbles);
                    mapBubbles.addAll(result.postCalculationProxyBubbles);

                    // Add bubbles
                    for (MapBubble mapBubble : mapBubbles) {
                        if (!mapBubble.isInProxy() && !bubbleMapLayer.getMapBubbles().contains(mapBubble)) {
                            bubbleMapLayer.add(mapBubble);
                        }
                    }

                    // Remove bubbles
                    for (MapBubble mapBubble : bubbleMapLayer.getMapBubbles()) {
                        if (!mapBubbles.contains(mapBubble)) {
                            result.preCalculationProxyBubbles.add(mapBubble);
                        } else if (mapBubble.isInProxy()) {
                            result.preCalculationProxyBubbles.add(mapBubble);
                        }
                    }

                    for (MapBubble mapBubble : result.preCalculationProxyBubbles) {
                        bubbleMapLayer.remove(mapBubble);
                    }

                    // Move bubbles
                    for (MapBubble mapBubble : bubbleMapLayer.getMapBubbles()) {
                        if (mapBubble.getRawLatLng() != null) {
                            bubbleMapLayer.move(mapBubble, mapBubble.getRawLatLng());
                            mapBubble.setRawLatLng(null);
                        }
                    }
                }, Throwable::printStackTrace);
    }

    private Single<MergeBubblesResult> mergeBubbles(final Set<MapBubble> mapBubbles, final double clusterSize) {
        return Single.fromCallable(() -> {
            MergeBubblesResult result = new MergeBubblesResult();
            ClusterMap clusterMap = new ClusterMap(clusterSize);

            for (MapBubble mapBubble : mapBubbles) {
                mapBubble.setInProxy(false);
                if (mapBubble.getType() == BubbleType.PROXY) {
                    result.preCalculationProxyBubbles.add(mapBubble);
                } else if (mapBubble.getType() == BubbleType.STATUS || mapBubble.getType() == BubbleType.EVENT || mapBubble.getType() == BubbleType.PHYSICAL_GROUP) {
                    if (mapBubble.isCanProxy()) {
                        clusterMap.add(mapBubble);
                    }
                }
            }

            List<Set<MapBubble>> clusters = clusterMap.generateClusters();

            for (Set<MapBubble> cluster : clusters) {
                MapBubble proxyMapBubble = new MapBubble(new LatLng(0, 0), BubbleType.PROXY);
                proxyMapBubble.setPinned(true);
                proxyMapBubble.proxies(cluster);
                float lat = 0, lng = 0;
                int num = 0;
                for (MapBubble mapBubble : cluster) {
                    mapBubble.setInProxy(true);
                    lat += mapBubble.getLatLng().latitude;
                    lng += mapBubble.getLatLng().longitude;
                    num++;
                }
                proxyMapBubble.setLatLng(new LatLng(
                        lat / num,
                        lng / num
                ));

                result.postCalculationProxyBubbles.add(proxyMapBubble);
            }

            return result;
        });
    }

    private double getClusterSize() {
        return abs(mapViewportCallback.getMapViewport().latLngBounds.southwest.longitude - mapViewportCallback.getMapViewport().latLngBounds.northeast.longitude) / MERGE_RESOLUTION;
    }

    public void add(final MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
        bubbleMapLayer.ensureBubbleView(mapBubble);
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

        boolean updated = false;

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
            updated = true;
        }

        for (MapBubble mapBubble : mapBubbles) {
            if (mapBubble.getPhone() != null && !updatedBubbles.contains(mapBubble.getPhone())) {
                this.mapBubbles.add(mapBubble);
                updated = true;
            }
        }

        if (updated || lastClusterSize != getClusterSize()) {
            recalculate();
        }
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

    public interface MapViewportCallback {
        VisibleRegion getMapViewport();
    }

    private static class MergeBubblesResult {
        Set<MapBubble> preCalculationProxyBubbles = new HashSet<>();
        Set<MapBubble> postCalculationProxyBubbles = new HashSet<>();
    }
}
