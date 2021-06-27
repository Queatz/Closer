package closer.vlllage.com.closer.handler.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import at.bluesource.choicesdk.maps.common.*
import at.bluesource.choicesdk.maps.common.Map
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import kotlin.math.min


class MapHandler constructor(private val on: On) : OnMapReadyCallback {

    private var map: Map? = null
    private var mapView: View? = null
    private var centerOnMapLoad: LatLng? = null
    var onMapChangedListener: (() -> Unit)? = null
    var onMapClickedListener: ((LatLng) -> Unit)? = null
    var onMapLongClickedListener: ((LatLng) -> Unit)? = null
    var onMapReadyListener: ((Map) -> Unit)? = null
    var onMapIdleListener: ((LatLng) -> Unit)? = null
    private var topPadding = on<WindowHandler>().statusBarHeight

    private val onMapIdleObservable = BehaviorSubject.create<CameraPosition>()
    private val onMapReadyObservable = BehaviorSubject.create<Map>()

    val center: LatLng?
        get() = if (map == null) {
            null
        } else map!!.cameraPosition.target

    val zoom: Float
        get() = if (map == null) {
            0f
        } else map!!.cameraPosition.zoom

    val visibleRegion: VisibleRegion?
        get() = map?.getProjection()?.getVisibleRegion()

    fun attach(fragmentManager: FragmentManager, @IdRes mapId: Int) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()

        val mapFragment: MapFragment = MapFragment.newInstance()
        fragmentTransaction.add(mapId, mapFragment)
        fragmentTransaction.commitNow()

        mapFragment.getMapAsync(this)
        mapView = mapFragment.view

        mapFragment.getMapObservable().observeOn(AndroidSchedulers.mainThread()).subscribe {
            mapView = mapFragment.view
            mapView!!.addOnLayoutChangeListener { v, i1, i2, i3, i4, i5, i6, i7, i8 -> onMapChangedListener!!.invoke() }
        }
    }

    fun centerMap(latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        centerOnMapLoad = latLng

        map?.animateCamera(CameraUpdateFactory.get().newLatLngZoom(latLng, zoom))
    }

    fun zoomMap(amount: Float) {
        map?.animateCamera(CameraUpdateFactory.get().zoomBy(amount))
    }

    override fun onMapReady(map: Map?) {
        this.map = map
        onMapReadyObservable.onNext(map!!)

        if (on<PersistenceHandler>().lastMapCenter != null) {
            map!!.moveCamera(CameraUpdateFactory.get().newCameraPosition(CameraPosition.Builder()
                    .setTarget(on<PersistenceHandler>().lastMapCenter!!)
                    .setTilt(DEFAULT_TILT)
                    .setZoom(DEFAULT_ZOOM)
                    .build()))
        }

        onMapReadyListener!!.invoke(map!!)
        map!!.setOnCameraMoveListener { this.mapChanged() }
        map!!.setOnMapClickListener { onMapClickedListener!!.invoke(it) }
        map!!.setOnMapLongClickListener { onMapLongClickedListener?.invoke(it) }
        map!!.setOnCameraIdleListener {
            on<PersistenceHandler>().lastMapCenter = map!!.cameraPosition.target
            onMapIdleListener!!.invoke(map!!.cameraPosition.target)
            onMapIdleObservable.onNext(map!!.cameraPosition)
        }
        onMapChangedListener!!.invoke()
        map!!.setPadding(
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                topPadding,
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf))
        map!!.getUiSettings().isMyLocationButtonEnabled = false

        if (centerOnMapLoad == null) {
            locateMe()
        } else {
            map!!.moveCamera(CameraUpdateFactory.get().newLatLngZoom(centerOnMapLoad!!, DEFAULT_ZOOM))
            centerOnMapLoad = null
        }

        updateMyLocationEnabled()
    }

    fun setTopPadding(padding: Int) {
        topPadding = padding
        map?.setPadding(
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                topPadding,
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf)
        )
    }

    fun locateMe() {
        on<LocationHandler>().getCurrentLocation { location: Location -> this.onLocationFound(location) }
    }

    @SuppressLint("MissingPermission")
    fun updateMyLocationEnabled() {
        map ?: return

        on<PermissionHandler>()
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .`when` { granted -> map!!.isMyLocationEnabled = granted }
    }

    private fun mapChanged() {
        onMapChangedListener!!.invoke()

        map ?: return

        // Huawei does not support satellite maps
        if (map!!.getHuaweiMap() != null) {
            if (map!!.mapType != Map.MAP_TYPE_NORMAL) {
                map!!.mapType = Map.MAP_TYPE_NORMAL
            }

            return
        }

        if (map!!.cameraPosition.zoom >= 18) {
            if (map!!.mapType != Map.MAP_TYPE_HYBRID) {
                map!!.mapType = Map.MAP_TYPE_HYBRID
            }
        } else if (map!!.cameraPosition.zoom <= 3) {
            if (map!!.mapType != Map.MAP_TYPE_HYBRID) {
                map!!.mapType = Map.MAP_TYPE_HYBRID
            }
        } else {
            if (map!!.mapType != Map.MAP_TYPE_NORMAL) {
                map!!.mapType = Map.MAP_TYPE_NORMAL
            }
        }
    }

    private fun onLocationFound(location: Location) {
        if (centerOnMapLoad != null) {
            centerOnMapLoad = null
            return
        }

        val latLng = LatLng(location.latitude, location.longitude)
        val cameraPosition = CameraPosition.Builder()
                .setTarget(latLng)
                .setZoom(DEFAULT_ZOOM)
                .setTilt(DEFAULT_TILT)
                .build()
        map!!.moveCamera(CameraUpdateFactory.get().newCameraPosition(cameraPosition))

        if (on<NightDayHandler>().isNight(Date(), location)) {
            // TODO ChoiceSDK map style
//            map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(on<ApplicationHandler>().app, closer.vlllage.com.closer.R.raw.google_maps_night_mode))
        }

        on<RefreshHandler>().refreshGroupActions(LatLng(location.latitude, location.longitude))
    }

    fun centerOn(mapBubbles: Collection<MapBubble>) {
        if (mapBubbles.isEmpty()) {
            return
        }

        val builder = LatLngBounds.getBuilder()
        for (mapBubble in mapBubbles) {
            builder.include(mapBubble.latLng!!)
        }

        try {
            val cu = CameraUpdateFactory.get().newLatLngBounds(builder.build(), min(
                    on<ActivityHandler>().activity!!.window.decorView.width,
                    on<ActivityHandler>().activity!!.window.decorView.height
            ) / 4)
            map!!.animateCamera(cu, 225, null)
        } catch (ignored: Throwable) {
            // com.google.maps.api.android.lib6.common.apiexception.c:
            // Error using newLatLngBounds(LatLngBounds, int):
            // View size is too small after padding is applied.
        }

    }

    fun onMapIdleObservable() = onMapIdleObservable.observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())!!
    fun onMapReadyObservable() = onMapReadyObservable.observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())!!

    companion object {
        const val DEFAULT_ZOOM = 18f
        const val DEFAULT_TILT = 45f
    }
}
