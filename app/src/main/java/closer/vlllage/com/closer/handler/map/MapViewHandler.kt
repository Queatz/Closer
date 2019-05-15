package closer.vlllage.com.closer.handler.map

import android.content.Intent
import androidx.fragment.app.Fragment

import com.queatz.on.On

class MapViewHandler constructor(private val on: On) {

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
