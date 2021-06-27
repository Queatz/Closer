package closer.vlllage.com.closer.handler.map

import android.graphics.Color
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import at.bluesource.choicesdk.maps.common.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.queatz.on.On

class DataVisualsHandler constructor(private val on: On) {

    private val heatmaps = mutableMapOf<Layer, Heatmap>()

    fun attach() {

    }

    fun setPhones(list: List<LatLng>) {
        setData(list, Layer.PHONE)
    }

    fun setGroups(list: List<LatLng>) {
        setData(list, Layer.GROUP)
    }

    private fun setData(list: List<LatLng>, layer: Layer) {
        if (list.isEmpty()) {
            heatmaps.remove(layer)?.overlay?.remove()

            return
        }

        // TODO ChoiceSDK
//        heatmaps[layer]?.tileProvider?.setData(list) ?: run {
//            val heatmap = Heatmap()
//            heatmaps[layer] = heatmap
//            heatmap.tileProvider = HeatmapTileProvider.Builder()
//                    .gradient(gradient(layer))
//                    .radius(30)
//                    .data(list)
//                    .build()
//
//            on<DisposableHandler>().add(on<MapHandler>().onMapReadyObservable()
//                    .subscribe { map ->
//                        heatmap.overlay?.remove()
//                        heatmap.overlay = map.addTileOverlay(TileOverlayOptions().tileProvider(heatmap.tileProvider))
//                    })
//        }
    }

    private fun gradient(layer: Layer) = when (layer) {
        Layer.PHONE -> Gradient(intArrayOf(Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)), floatArrayOf(0.1f, 1.0f))
        Layer.GROUP -> Gradient(intArrayOf(Color.rgb(102, 225, 0), Color.rgb(255, 0, 0)), floatArrayOf(0.1f, 1.0f))
    }
}

internal enum class Layer {
    PHONE,
    GROUP
}

internal class Heatmap {
    lateinit var tileProvider: HeatmapTileProvider
    var overlay: TileOverlay? = null
}