package closer.vlllage.com.closer.handler.map

import android.app.Fragment
import android.content.Intent

import closer.vlllage.com.closer.pool.PoolMember

class MapViewHandler : PoolMember() {

    private var mapSlideFragment: MapSlideFragment? = null
    var onRequestMapOnScreenListener: (() -> Unit)? = null

    val mapFragment: Fragment
        get() {
            if (mapSlideFragment == null) {
                mapSlideFragment = MapSlideFragment()
            }
            return mapSlideFragment!!
        }

    fun onBackPressed(callback: (Boolean) -> Unit) {
        mapSlideFragment?.post(Runnable { callback.invoke(mapSlideFragment!!.onBackPressed()) })
    }

    fun handleIntent(intent: Intent) {
        mapSlideFragment?.post(Runnable { mapSlideFragment!!.handleIntent(intent, onRequestMapOnScreenListener) })
    }
}
