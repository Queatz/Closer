package closer.vlllage.com.closer.handler.bubble

import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class BubbleHandler : PoolMember() {

    private var onClickListener: ((MapBubble) -> Unit)? = null
    private lateinit var onMenuItemClickListener: OnMapBubbleMenuItemClickListener
    private lateinit var onMapBubbleSuggestionClickListener: MapBubbleSuggestionClickListener
    private lateinit var onMapBubbleEventClickListener: MapBubbleEventClickListener
    private lateinit var onMapBubblePhysicalGroupClickListener: MapBubblePhysicalGroupClickListener

    private val bubbleMapLayer = BubbleMapLayer()
    private val bubbleProxyLayer = BubbleProxyLayer(bubbleMapLayer) { `$`(MapHandler::class.java).visibleRegion!! }

    private val bubbleView: BubbleMapLayer.BubbleView
        get() = object : BubbleMapLayer.BubbleView {
            override fun createView(view: ViewGroup?, mapBubble: MapBubble): View {
                return when (mapBubble.type) {
                    BubbleType.PROXY -> `$`(MapBubbleProxyView::class.java).from(view!!, mapBubble) { proxiedMapBubble ->
                        when (proxiedMapBubble.type) {
                            BubbleType.STATUS -> onClickListener!!.invoke(proxiedMapBubble)
                            BubbleType.EVENT -> onMapBubbleEventClickListener.invoke(proxiedMapBubble)
                            BubbleType.PHYSICAL_GROUP -> onMapBubblePhysicalGroupClickListener.invoke(proxiedMapBubble)
                        }
                    }
                    BubbleType.MENU -> `$`(MapBubbleMenuView::class.java).from(view!!, mapBubble, onMenuItemClickListener)
                    BubbleType.SUGGESTION -> `$`(MapBubbleSuggestionView::class.java).from(view!!, mapBubble, onMapBubbleSuggestionClickListener)
                    BubbleType.EVENT -> `$`(MapBubbleEventView::class.java).from(view!!, mapBubble, onMapBubbleEventClickListener)
                    BubbleType.PHYSICAL_GROUP -> `$`(MapBubblePhysicalGroupView::class.java).from(view!!, mapBubble, onMapBubblePhysicalGroupClickListener)
                    else -> `$`(MapBubbleView::class.java).from(view!!, mapBubble, onClickListener!!)
                }
            }

            override fun updateView(mapBubble: MapBubble) {
                when (mapBubble.type) {
                    BubbleType.STATUS -> `$`(MapBubbleView::class.java).update(mapBubble.view!!, mapBubble)
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

    fun attach(map: GoogleMap) {
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
