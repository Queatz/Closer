package closer.vlllage.com.closer

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import closer.vlllage.com.closer.handler.PersonalSlideFragment
import closer.vlllage.com.closer.handler.map.MapViewHandler
import closer.vlllage.com.closer.handler.settings.SettingsSlideFragment
import closer.vlllage.com.closer.pool.PoolActivity
import com.github.queatz.slidescreen.SlideScreen
import com.github.queatz.slidescreen.SlideScreenAdapter

class MapsActivity : PoolActivity() {

    private lateinit var slideScreen: SlideScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.slidescreen)
        slideScreen = findViewById(R.id.slideScreen)
        slideScreen.adapter = object : SlideScreenAdapter {
            override fun getCount(): Int {
                return NUM_SCREENS
            }

            override fun getSlide(position: Int): Fragment {
                return when (position) {
                    POSITION_SCREEN_PERSONAL -> PersonalSlideFragment()
                    POSITION_SCREEN_MAP -> `$`(MapViewHandler::class.java).mapFragment
                    POSITION_SCREEN_SETTINGS -> SettingsSlideFragment()
                    else -> throw IndexOutOfBoundsException()
                }
            }

            override fun getFragmentManager() = this@MapsActivity.fragmentManager
        }

        if (intent != null) {
            onNewIntent(intent)
        }

        `$`(MapViewHandler::class.java).setOnRequestMapOnScreenListener(object : MapViewHandler.OnRequestMapOnScreenListener {
            override fun onRequestMapOnScreen() {
                slideScreen.slide = POSITION_SCREEN_MAP
            }
        })

        slideScreen.slide = POSITION_SCREEN_MAP
    }

    override fun onNewIntent(intent: Intent) {
        if (intent.hasExtra(EXTRA_SCREEN)) {
            when (intent.getStringExtra(EXTRA_SCREEN)) {
                EXTRA_SCREEN_PERSONAL -> {
                    slideScreen.slide = POSITION_SCREEN_PERSONAL
                    return
                }
                EXTRA_SCREEN_MAP -> {
                    slideScreen.slide = POSITION_SCREEN_MAP
                    return
                }
                EXTRA_SCREEN_SETTINGS -> {
                    slideScreen.slide = POSITION_SCREEN_SETTINGS
                    return
                }
            }
        }

        `$`(MapViewHandler::class.java).handleIntent(intent)
    }

    override fun onBackPressed() {
        `$`(MapViewHandler::class.java).onBackPressed {
            if (it) {
                return@onBackPressed
            }

            if (!slideScreen.isExpose) {
                slideScreen.expose(true)
                return@onBackPressed
            }

            super.onBackPressed()
        }
    }

    companion object {
        const val EXTRA_LAT_LNG = "latLng"
        const val EXTRA_SUGGESTION = "suggestion"
        const val EXTRA_EVENT_ID = "eventId"
        const val EXTRA_NAME = "name"
        const val EXTRA_STATUS = "status"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_SCREEN_MAP = "screen.map"
        const val EXTRA_SCREEN_PERSONAL = "screen.personal"
        const val EXTRA_SCREEN_SETTINGS = "screen.settings"

        const val POSITION_SCREEN_PERSONAL = 0
        const val POSITION_SCREEN_MAP = 1
        const val POSITION_SCREEN_SETTINGS = 2
        const val NUM_SCREENS = 3
    }
}
