package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import closer.vlllage.com.closer.databinding.MainBinding
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ScanQrCodeHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.handler.map.MapViewHandler
import closer.vlllage.com.closer.handler.welcome.WelcomeSlideFragment
import closer.vlllage.com.closer.pool.PoolActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MapsActivity : PoolActivity() {

    private lateinit var fragmentContainer: FragmentContainerView

    private val accessDisposableGroup = on<DisposableHandler>().group()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentContainer = MainBinding.inflate(layoutInflater).let {
            setContentView(it.root)
            it.fragmentContainer
        }

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

        on<MapViewHandler>().handleIntent(intent)
    }

    override fun onBackPressed() {
        if (on<PersistenceHandler>().access) {
            on<MapViewHandler>().onBackPressed {
                if (it) {
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
            show(WelcomeSlideFragment())
        } else {
            show(on<MapViewHandler>().mapFragment)
        }
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commitAllowingStateLoss()
    }

    companion object {
        const val EXTRA_LAT_LNG = "latLng"
        const val EXTRA_ZOOM = "latLngZoom"
        const val EXTRA_SUGGESTION = "suggestion"
        const val EXTRA_EVENT_ID = "eventId"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_PROMPT = "prompt"
    }
}
