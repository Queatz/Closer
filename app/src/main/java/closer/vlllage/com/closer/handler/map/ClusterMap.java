package closer.vlllage.com.closer.handler.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import closer.vlllage.com.closer.handler.bubble.MapBubble;
import smile.neighbor.KDTree;
import smile.neighbor.Neighbor;

/**
 * Clusters map bubbles
 *
 * How it works:
 *  1) Choose a random bubble
 *  2) Keep adding the nearest neighbor until distance from cluster center is greater than the max allowed
 */
public class ClusterMap {

    private final double maxDistance;
    private final List<MapBubble> mapBubbles = new ArrayList<>();

    public ClusterMap(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void add(MapBubble mapBubble) {
        mapBubbles.add(mapBubble);
    }

    public List<Set<MapBubble>> generateClusters() {
        List<double[]> keys = new ArrayList<>();
        for (MapBubble mapBubble : mapBubbles) { keys.add(ll(mapBubble)); }
        double[][] keysAsArray = new double[keys.size()][];
        keys.toArray(keysAsArray);
        MapBubble[] mapBubblesAsArray = new MapBubble[mapBubbles.size()];
        mapBubbles.toArray(mapBubblesAsArray);
        KDTree<MapBubble> kdTree = new KDTree<>(keysAsArray, mapBubblesAsArray);
        List<Set<MapBubble>> result = new ArrayList<>();

        while (!mapBubbles.isEmpty()) {
            MapBubble mapBubble = mapBubbles.remove(0);
            Set<MapBubble> cluster = new HashSet<>();
            double[] clusterCenter = ll(mapBubble);
            List<Neighbor<double[], MapBubble>> nearbyMapBubbles = new ArrayList<>();
            kdTree.range(clusterCenter, maxDistance, nearbyMapBubbles);

            for (Neighbor<double[], MapBubble> neighbor : nearbyMapBubbles) {
                mapBubbles.remove(neighbor.value);
                cluster.add(neighbor.value);
            }

            result.add(cluster);
        }

        return result;
    }

    private double[] ll(MapBubble mapBubble) {
        return new double[] { mapBubble.getLatLng().latitude, mapBubble.getLatLng().longitude };
    }
}
