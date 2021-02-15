package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import closer.vlllage.com.closer.handler.PersonalSlideFragment
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.handler.map.MapViewHandler
import closer.vlllage.com.closer.handler.settings.SettingsSlideFragment
import closer.vlllage.com.closer.handler.welcome.WelcomeSlideFragment
import closer.vlllage.com.closer.pool.PoolActivity
import com.github.queatz.slidescreen.SlideScreen
import com.github.queatz.slidescreen.SlideScreenAdapter
import io.reactivex.android.schedulers.AndroidSchedulers

class MapsActivity : PoolActivity() {

    private lateinit var slideScreen: SlideScreen

    private val accessDisposableGroup = on<DisposableHandler>().group()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.slidescreen)
        slideScreen = findViewById(R.id.slideScreen)
        refreshScreens()

        if (intent != null) {
            onNewIntent(intent)
        }

        if (on<PersistenceHandler>().access.not()) {
            on<PersistenceHandler>().changes
                    .filter { it == PersistenceHandler.PREFERENCE_ACCESS }
                    .map { on<PersistenceHandler>().access }
                    .filter { it }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        refreshScreens()
                        accessDisposableGroup.dispose()
                    }.also {
                        accessDisposableGroup.add(it)
                    }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let {
                if (it.host == "closer.group") on<ScanQrCodeHandler>().handleResult(it)
            }
        }

        if (intent.hasExtra(EXTRA_PROMPT)) {
            on<ToastHandler>().show(intent.getStringExtra(EXTRA_PROMPT)!!)
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
        if (on<PersistenceHandler>().access) {
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
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshScreens() {
        if (on<PersistenceHandler>().access.not()) {
            slideScreen.adapter = object : SlideScreenAdapter {
                override fun getCount() = 1
                override fun getSlide(position: Int) = WelcomeSlideFragment()
                override fun getFragmentManager() = this@MapsActivity.supportFragmentManager
            }
        } else {
            slideScreen.adapter = object : SlideScreenAdapter {
                override fun getCount() = NUM_SCREENS

                override fun getSlide(position: Int) = when (position) {
                    POSITION_SCREEN_PERSONAL -> PersonalSlideFragment()
                    POSITION_SCREEN_MAP -> on<MapViewHandler>().mapFragment
                    POSITION_SCREEN_SETTINGS -> SettingsSlideFragment()
                    else -> throw IndexOutOfBoundsException()
                }

                override fun getFragmentManager() = this@MapsActivity.supportFragmentManager
            }

            on<MapViewHandler>().onRequestMapOnScreenListener = {
                slideScreen.slide = POSITION_SCREEN_MAP
            }

            slideScreen.slide = POSITION_SCREEN_MAP
        }
    }

    companion object {
        const val EXTRA_LAT_LNG = "latLng"
        const val EXTRA_ZOOM = "latLngZoom"
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
