package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import closer.vlllage.com.closer.handler.PersonalSlideFragment
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
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
                    POSITION_SCREEN_MAP -> on<MapViewHandler>().mapFragment
                    POSITION_SCREEN_SETTINGS -> SettingsSlideFragment()
                    else -> throw IndexOutOfBoundsException()
                }
            }

            override fun getFragmentManager() = this@MapsActivity.supportFragmentManager
        }

        if (intent != null) {
            onNewIntent(intent)
        }

        on<MapViewHandler>().onRequestMapOnScreenListener = {
            slideScreen.slide = POSITION_SCREEN_MAP
        }

        slideScreen.slide = POSITION_SCREEN_MAP
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                if (it.host == "closer.group") on<ScanQrCodeHandler>().handleResult(it)
            }
        }

        if (intent.hasExtra(EXTRA_PROMPT)) {
            on<DefaultAlerts>().message(intent.getStringExtra(EXTRA_PROMPT))
        }

        if (intent.hasExtra(EXTRA_SCREEN)) {
            when (intent.getStringExtra(EXTRA_SCREEN)) {
                EXTRA_SCREEN_PERSONAL -> {
                    slideScreen.slide = POSITION_SCREEN_PERSONAL
                }
                EXTRA_SCREEN_MAP -> {
                    slideScreen.slide = POSITION_SCREEN_MAP
                }
                EXTRA_SCREEN_SETTINGS -> {
                    slideScreen.slide = POSITION_SCREEN_SETTINGS
                }
            }
        }

        on<MapViewHandler>().handleIntent(intent)
    }

    override fun onBackPressed() {
        on<MapViewHandler>().onBackPressed {
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
        const val EXTRA_PHONE = "phone"
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_PROMPT = "prompt"
        const val EXTRA_SCREEN_MAP = "screen.map"
        const val EXTRA_SCREEN_PERSONAL = "screen.personal"
        const val EXTRA_SCREEN_SETTINGS = "screen.settings"

        const val POSITION_SCREEN_PERSONAL = 0
        const val POSITION_SCREEN_MAP = 1
        const val POSITION_SCREEN_SETTINGS = 2
        const val NUM_SCREENS = 3
    }
}
