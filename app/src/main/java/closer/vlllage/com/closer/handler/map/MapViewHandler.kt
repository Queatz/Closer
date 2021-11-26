package closer.vlllage.com.closer.handler.map

import android.content.Intent
import androidx.fragment.app.Fragment

import com.queatz.on.On

class MapViewHandler constructor(private val on: On) {

    private val mapSlideFragment by lazy { MapSlideFragment() }
    var onRequestMapOnScreenListener: (() -> Unit) = {}

    val mapFragment: Fragment = mapSlideFragment

    fun onBackPressed(callback: (Boolean) -> Unit) {
        mapSlideFragment.post { callback.invoke(mapSlideFragment.onBackPressed()) }
    }

    fun handleIntent(intent: Intent) {
        mapSlideFragment.post { mapSlideFragment.handleIntent(intent, onRequestMapOnScreenListener) }
    }
}
