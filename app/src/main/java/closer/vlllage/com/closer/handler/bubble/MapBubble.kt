package closer.vlllage.com.closer.handler.bubble

import android.view.View
import com.google.android.gms.maps.model.LatLng
import java.util.*

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
    var onItemClickListener: OnItemClickListener? = null
    var onViewReadyListener: OnViewReadyListener? = null
    var tag: Any? = null
    val proxies = ArrayList<MapBubble>()

    constructor(latLng: LatLng?, name: String?, status: String?) {
        this.latLng = latLng
        this.name = name
        this.status = status
    }

    constructor(latLng: LatLng, type: BubbleType) {
        this.latLng = latLng
        this.type = type
        isPinned = true
        isOnTop = true
    }

    fun addProxies(proxiedBubbles: Collection<MapBubble>) {
        this.proxies.addAll(proxiedBubbles)
    }

    fun updateFrom(mapBubble: MapBubble) {
        this.phone = mapBubble.phone
        this.name = mapBubble.name
        this.status = mapBubble.status
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    interface OnViewReadyListener {
        fun onViewReady(view: View)
    }
}
