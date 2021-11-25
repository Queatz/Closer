package closer.vlllage.com.closer.handler.bubble

import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.handler.map.MapHandler
import at.bluesource.choicesdk.maps.common.LatLng
import at.bluesource.choicesdk.maps.common.Map
import closer.vlllage.com.closer.databinding.MapBubblePhysicalGroupBinding
import com.queatz.on.On

class BubbleHandler constructor(private val on: On) {

    private var onClickListener: ((MapBubble) -> Unit)? = null
    private lateinit var onMenuItemClickListener: OnMapBubbleMenuItemClickListener
    private lateinit var onMapBubbleSuggestionClickListener: MapBubbleSuggestionClickListener
    private lateinit var onMapBubbleEventClickListener: MapBubbleEventClickListener
    private lateinit var onMapBubblePhysicalGroupClickListener: MapBubblePhysicalGroupClickListener

    private val bubbleMapLayer = BubbleMapLayer(on)
    private val bubbleProxyLayer = BubbleProxyLayer(bubbleMapLayer) { on<MapHandler>().visibleRegion }

    private val bubbleView: BubbleMapLayer.BubbleView
        get() = object : BubbleMapLayer.BubbleView {
            override fun createView(view: ViewGroup?, mapBubble: MapBubble): View {
                return when (mapBubble.type) {
                    BubbleType.PROXY -> on<MapBubbleProxyView>().from(view!!, mapBubble) { proxiedMapBubble ->
                        when (proxiedMapBubble.type) {
                            BubbleType.STATUS -> onClickListener!!.invoke(proxiedMapBubble)
                            BubbleType.EVENT -> onMapBubbleEventClickListener.invoke(proxiedMapBubble)
                            BubbleType.PHYSICAL_GROUP -> onMapBubblePhysicalGroupClickListener.invoke(proxiedMapBubble)
                            BubbleType.SUGGESTION -> onMapBubbleSuggestionClickListener.invoke(proxiedMapBubble)
                        }
                    }
                    BubbleType.MENU -> on<MapBubbleMenuView>().from(view!!, mapBubble, onMenuItemClickListener)
                    BubbleType.SUGGESTION -> on<MapBubbleSuggestionView>().from(view!!, mapBubble, onMapBubbleSuggestionClickListener)
                    BubbleType.EVENT -> on<MapBubbleEventView>().from(view!!, mapBubble, onMapBubbleEventClickListener)
                    BubbleType.PHYSICAL_GROUP -> on<MapBubblePhysicalGroupView>().from(view!!, mapBubble, onMapBubblePhysicalGroupClickListener).root
                    else -> on<MapBubblePhysicalGroupView>().from(view!!, mapBubble, onClickListener!!).root
                }
            }

            override fun updateView(mapBubble: MapBubble) {
                when (mapBubble.type) {
                    BubbleType.STATUS -> on<MapBubblePhysicalGroupView>().update(MapBubblePhysicalGroupBinding.bind(mapBubble.view!!), mapBubble)
                }
            }
        }

    fun attach(bubbleMapLayerLayout: ViewGroup,
               onClickListener: (MapBubble) -> Unit,
               onMenuItemClickListener: OnMapBubbleMenuItemClickListener,
               onMapBubbleEventClickListener: MapBubbleEventClickListener,
               onMapBubbleSuggestionClickListener: MapBubbleSuggestionClickListener,
               onMapBubblePhysicalGroupClickListener: MapBubblePhysicalGroupClickListener) {
        this.onClickListener = onClickListener
        this.onMenuItemClickListener = onMenuItemClickListener
        this.onMapBubbleEventClickListener = onMapBubbleEventClickListener
        this.onMapBubbleSuggestionClickListener = onMapBubbleSuggestionClickListener
        this.onMapBubblePhysicalGroupClickListener = onMapBubblePhysicalGroupClickListener
        bubbleMapLayer.attach(bubbleMapLayerLayout, bubbleView)
    }

    fun attach(map: Map) {
        bubbleMapLayer.attach(map)
    }

    fun add(mapBubble: MapBubble) {
        bubbleProxyLayer.add(mapBubble)
    }

    fun replace(mapBubbles: List<MapBubble>) {
        bubbleProxyLayer.replace(mapBubbles)
    }

    fun move(mapBubble: MapBubble, latLng: LatLng) {
        bubbleProxyLayer.move(mapBubble, latLng)
    }

    fun remove(mapBubble: MapBubble) {
        bubbleProxyLayer.remove(mapBubble)
    }

    fun remove(callback: (MapBubble) -> Boolean): Boolean {
        return bubbleProxyLayer.remove(callback)
    }

    fun update() {
        bubbleMapLayer.update()
    }

    fun update(mapBubble: MapBubble) {
        bubbleMapLayer.update(mapBubble)
    }

    fun updateDetails(mapBubble: MapBubble) {
        bubbleMapLayer.updateDetails(mapBubble)
    }
}
