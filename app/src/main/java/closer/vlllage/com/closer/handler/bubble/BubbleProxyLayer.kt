package closer.vlllage.com.closer.handler.bubble

import closer.vlllage.com.closer.handler.map.ClusterMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.Math.abs
import java.util.*

class BubbleProxyLayer(private val bubbleMapLayer: BubbleMapLayer, private val mapViewportCallback: () -> VisibleRegion) {

    private val mapBubbles = HashSet<MapBubble>()
    private var lastClusterSize: Double = 0.toDouble()
    private var mergeBubblesDisposable: Disposable? = null

    private val clusterSize: Double
        get() = if (mapViewportCallback == null || mapViewportCallback.invoke() == null) {
            1.0
        } else abs(mapViewportCallback.invoke()!!.latLngBounds.southwest.longitude - mapViewportCallback.invoke()!!.latLngBounds.northeast.longitude) / MERGE_RESOLUTION

    fun recalculate() {
        lastClusterSize = clusterSize

        if (mergeBubblesDisposable != null) {
            mergeBubblesDisposable!!.dispose()
        }

        mergeBubblesDisposable = mergeBubbles(HashSet(mapBubbles), lastClusterSize)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    // TODO check if proxies are the same and don't replace
                    mapBubbles.removeAll(result.preCalculationProxyBubbles)
                    mapBubbles.addAll(result.postCalculationProxyBubbles)

                    // Add bubbles
                    for (mapBubble in mapBubbles) {
                        if (!mapBubble.inProxy && !bubbleMapLayer.mapBubbles.contains(mapBubble)) {
                            bubbleMapLayer.add(mapBubble)
                        }
                    }

                    // Remove bubbles
                    for (mapBubble in bubbleMapLayer.mapBubbles) {
                        if (!mapBubbles.contains(mapBubble)) {
                            result.preCalculationProxyBubbles.add(mapBubble)
                        } else if (mapBubble.inProxy) {
                            result.preCalculationProxyBubbles.add(mapBubble)
                        }
                    }

                    for (mapBubble in result.preCalculationProxyBubbles) {
                        bubbleMapLayer.remove(mapBubble)
                    }

                    // Move bubbles
                    for (mapBubble in bubbleMapLayer.mapBubbles) {
                        if (mapBubble.rawLatLng != null) {
                            bubbleMapLayer.move(mapBubble, mapBubble.rawLatLng!!)
                            mapBubble.rawLatLng = null
                        }
                    }
                }, { it.printStackTrace() })
    }

    private fun mergeBubbles(mapBubbles: Set<MapBubble>, clusterSize: Double): Single<MergeBubblesResult> {
        return Single.fromCallable {
            val result = MergeBubblesResult()
            val clusterMap = ClusterMap(clusterSize)

            for (mapBubble in mapBubbles) {
                mapBubble.inProxy = false
                if (mapBubble.type == BubbleType.PROXY) {
                    result.preCalculationProxyBubbles.add(mapBubble)
                } else if (mapBubble.type == BubbleType.STATUS || mapBubble.type == BubbleType.EVENT || mapBubble.type == BubbleType.PHYSICAL_GROUP) {
                    if (mapBubble.canProxy) {
                        clusterMap.add(mapBubble)
                    }
                }
            }

            val clusters = clusterMap.generateClusters()

            for (cluster in clusters) {
                val proxyMapBubble = MapBubble(LatLng(0.0, 0.0), BubbleType.PROXY)
                proxyMapBubble.isPinned = true
                proxyMapBubble.addProxies(cluster)
                var lat = 0f
                var lng = 0f
                var num = 0
                for (mapBubble in cluster) {
                    mapBubble.inProxy = true
                    lat += mapBubble.latLng!!.latitude.toFloat()
                    lng += mapBubble.latLng!!.longitude.toFloat()
                    num++
                }
                proxyMapBubble.latLng = LatLng(
                        (lat / num).toDouble(),
                        (lng / num).toDouble()
                )

                result.postCalculationProxyBubbles.add(proxyMapBubble)
            }

            val preHashes = HashMap<Long, MapBubble>()
            val postHashes = HashMap<Long, MapBubble>()

            for (mapBubble in result.preCalculationProxyBubbles) {
                preHashes[proxyHash(mapBubble)] = mapBubble
            }

            for (mapBubble in result.postCalculationProxyBubbles) {
                postHashes[proxyHash(mapBubble)] = mapBubble
            }

            for (hash in preHashes.keys) {
                if (postHashes.containsKey(hash)) {
                    result.preCalculationProxyBubbles.remove(preHashes[hash])
                    result.postCalculationProxyBubbles.remove(postHashes[hash])
                }
            }

            result
        }
    }

    private fun proxyHash(proxyMapBubble: MapBubble): Long {
        var hash: Long = 0
        for (mapBubble in proxyMapBubble.proxies) {
            hash += mapBubble.hashCode().toLong()
        }
        return hash
    }

    fun add(mapBubble: MapBubble) {
        mapBubbles.add(mapBubble)
        recalculate()
    }

    fun replace(mapBubbles: List<MapBubble>) {
        val byPhone = HashMap<String, MapBubble>()

        for (mapBubble in mapBubbles) {
            if (mapBubble.phone != null) {
                byPhone[mapBubble.phone!!] = mapBubble
            }
        }

        val updatedBubbles = HashSet<String>()
        val toRemoveBubbles = HashSet<MapBubble>()

        var updated = false

        for (mapBubble in this.mapBubbles) {
            if (mapBubble.isPinned) {
                continue
            }

            if (mapBubble.phone == null || !byPhone.containsKey(mapBubble.phone!!)) {
                toRemoveBubbles.add(mapBubble)
            } else {
                mapBubble.updateFrom(byPhone[mapBubble.phone!!]!!)
                mapBubble.rawLatLng = byPhone[mapBubble.phone!!]!!.latLng
                bubbleMapLayer.updateDetails(mapBubble)
                updatedBubbles.add(mapBubble.phone!!)
            }
        }

        for (mapBubble in toRemoveBubbles) {
            this.mapBubbles.remove(mapBubble)
            updated = true
        }

        for (mapBubble in mapBubbles) {
            if (mapBubble.phone != null && !updatedBubbles.contains(mapBubble.phone!!)) {
                this.mapBubbles.add(mapBubble)
                updated = true
            }
        }

        if (updated || lastClusterSize != clusterSize) {
            recalculate()
        }
    }

    fun remove(mapBubble: MapBubble) {
        mapBubbles.remove(mapBubble)
        recalculate()
    }

    fun remove(callback: (MapBubble) -> Boolean): Boolean {
        val toRemove = HashSet<MapBubble>()
        for (mapBubble in mapBubbles) {
            if (callback.invoke(mapBubble)) {
                toRemove.add(mapBubble)
            }
        }

        if (toRemove.isEmpty()) {
            return false
        }

        for (mapBubble in toRemove) {
            mapBubbles.remove(mapBubble)
        }

        recalculate()
        return true
    }

    fun move(mapBubble: MapBubble, latLng: LatLng) {
        mapBubble.rawLatLng = latLng
        recalculate()
    }


    private class MergeBubblesResult {
        internal var preCalculationProxyBubbles: MutableSet<MapBubble> = HashSet()
        internal var postCalculationProxyBubbles: MutableSet<MapBubble> = HashSet()
    }

    companion object {

        private val MERGE_RESOLUTION = 1.5
    }
}
