package closer.vlllage.com.closer.handler.map

import android.app.Fragment
import android.content.Intent

import closer.vlllage.com.closer.pool.PoolMember

class MapViewHandler : PoolMember() {

    private var mapSlideFragment: MapSlideFragment? = null
    private var onRequestMapOnScreenListener: OnRequestMapOnScreenListener? = null

    val mapFragment: Fragment
        get() {
            if (mapSlideFragment == null) {
                mapSlideFragment = MapSlideFragment()
            }
            return mapSlideFragment!!
        }

    fun onBackPressed(callback: OnBackPressedCallback) {
        mapSlideFragment!!.post(Runnable { callback.onBackPressedResult(mapSlideFragment!!.onBackPressed()) })
    }

    fun handleIntent(intent: Intent) {
        mapSlideFragment!!.post(Runnable { mapSlideFragment!!.handleIntent(intent, onRequestMapOnScreenListener!!) })
    }

    fun setOnRequestMapOnScreenListener(onRequestMapOnScreenListener: OnRequestMapOnScreenListener): MapViewHandler {
        this.onRequestMapOnScreenListener = onRequestMapOnScreenListener
        return this
    }

    interface OnBackPressedCallback {
        fun onBackPressedResult(result: Boolean)
    }

    interface OnRequestMapOnScreenListener {
        fun onRequestMapOnScreen()
    }
}
