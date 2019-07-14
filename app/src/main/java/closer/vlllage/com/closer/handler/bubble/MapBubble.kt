package closer.vlllage.com.closer.handler.bubble

import android.view.View
import com.google.android.gms.maps.model.LatLng

/**
 * Created by jacob on 2/18/18.
 */

class MapBubble {
    var phone: String? = null
    var latLng: LatLng? = null
    var rawLatLng: LatLng? = null
        get() = if (field != null) {
            field
        } else latLng
    var inProxy: Boolean = false
    var canProxy = true
    var name: String? = null
    var status: String? = null
    var view: View? = null
    var isPinned: Boolean = false
    var action: String? = null
    var isOnTop: Boolean = false
    var type = BubbleType.STATUS
    var onItemClickListener: ((position: Int) -> Unit)? = null
    var onViewReadyListener: ((view: View) -> Unit)? = null
    var tag: Any? = null
    val proxies = mutableListOf<MapBubble>()

    constructor(latLng: LatLng?, name: String?, status: String?) {
        this.latLng = latLng
        this.name = name
        this.status = status
    }

    constructor(latLng: LatLng, type: BubbleType, isPinned: Boolean = false, isOnTop: Boolean = false) {
        this.latLng = latLng
        this.type = type
        this.isPinned = isPinned
        this.isOnTop = isOnTop
    }

    fun addProxies(proxiedBubbles: Collection<MapBubble>) {
        this.proxies.addAll(proxiedBubbles)
    }

    fun updateFrom(mapBubble: MapBubble) {
        this.phone = mapBubble.phone
        this.name = mapBubble.name
        this.status = mapBubble.status
    }
}
