package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.handler.bubble.MapBubble
import smile.neighbor.KDTree
import smile.neighbor.Neighbor
import java.util.*

/**
 * Clusters map bubbles
 *
 * How it works:
 * 1) Choose a random bubble
 * 2) Keep adding the nearest neighbor until distance from cluster center is greater than the max allowed
 */
class ClusterMap(private val maxDistance: Double) {
    private val mapBubbles = ArrayList<MapBubble>()

    fun add(mapBubble: MapBubble) {
        mapBubbles.add(mapBubble)
    }

    fun generateClusters(): List<Set<MapBubble>> {
        val result = ArrayList<Set<MapBubble>>()

        if (mapBubbles.isEmpty() || maxDistance == 0.0) {
            return result
        }

        val keys = ArrayList<DoubleArray>()
        for (mapBubble in mapBubbles) {
            keys.add(ll(mapBubble))
        }
        val kdTree = KDTree<MapBubble>(keys.toTypedArray(), mapBubbles.toTypedArray())

        while (mapBubbles.isNotEmpty()) {
            val mapBubble = mapBubbles.removeAt(0)
            val cluster = HashSet<MapBubble>()
            cluster.add(mapBubble)
            val clusterCenter = ll(mapBubble)
            val nearbyMapBubbles = ArrayList<Neighbor<DoubleArray, MapBubble>>()
            kdTree.range(clusterCenter, maxDistance, nearbyMapBubbles)

            for (neighbor in nearbyMapBubbles) {
                if (mapBubbles.contains(neighbor.value)) {
                    mapBubbles.remove(neighbor.value)
                    cluster.add(neighbor.value)
                }
            }

            if (cluster.size > 1) {
                result.add(cluster)
            }
        }

        return result
    }

    private fun ll(mapBubble: MapBubble): DoubleArray = doubleArrayOf(
            mapBubble.latLng!!.latitude,
            mapBubble.latLng!!.longitude
    )
}
