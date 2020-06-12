package closer.vlllage.com.closer.handler.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.view.View
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_maps.view.*
import java.util.*


class MapHandler constructor(private val on: On) : OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var mapView: View? = null
    private var centerOnMapLoad: LatLng? = null
    var onMapChangedListener: (() -> Unit)? = null
    var onMapClickedListener: ((LatLng) -> Unit)? = null
    var onMapLongClickedListener: ((LatLng) -> Unit)? = null
    var onMapReadyListener: ((GoogleMap) -> Unit)? = null
    var onMapIdleListener: ((LatLng) -> Unit)? = null
    private var topPadding = on<WindowHandler>().statusBarHeight

    private val onMapIdleObservable = BehaviorSubject.create<CameraPosition>()
    private val onMapReadyObservable = BehaviorSubject.create<GoogleMap>()

    val center: LatLng?
        get() = if (map == null) {
            null
        } else map!!.cameraPosition.target

    val zoom: Float
        get() = if (map == null) {
            0f
        } else map!!.cameraPosition.zoom

    val visibleRegion: VisibleRegion?
        get() = map?.projection?.visibleRegion

    fun attach(mapFragment: SupportMapFragment) {
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view
    }

    fun centerMap(latLng: LatLng, zoom: Float = DEFAULT_ZOOM) {
        centerOnMapLoad = latLng

        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun zoomMap(amount: Float) {
        map?.animateCamera(CameraUpdateFactory.zoomBy(amount))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        onMapReadyObservable.onNext(map!!)

        if (on<PersistenceHandler>().lastMapCenter != null) {
            map!!.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                    .target(on<PersistenceHandler>().lastMapCenter!!)
                    .tilt(DEFAULT_TILT)
                    .zoom(DEFAULT_ZOOM)
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
        mapView!!.addOnLayoutChangeListener { v, i1, i2, i3, i4, i5, i6, i7, i8 -> onMapChangedListener!!.invoke() }
        onMapChangedListener!!.invoke()
        map!!.setPadding(
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                topPadding,
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf),
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf))
        map!!.uiSettings.isMyLocationButtonEnabled = false

        if (centerOnMapLoad == null) {
            locateMe()
        } else {
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOnMapLoad, DEFAULT_ZOOM))
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
        if (map == null) {
            return
        }

        on<PermissionHandler>()
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .`when` { granted -> map!!.isMyLocationEnabled = granted }
    }

    private fun mapChanged() {
        onMapChangedListener!!.invoke()

        if (map == null) {
            return
        }

        if (map!!.cameraPosition.zoom >= 18) {
            if (map!!.mapType != GoogleMap.MAP_TYPE_HYBRID) {
                map!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
        } else if (map!!.cameraPosition.zoom <= 3) {
            if (map!!.mapType != GoogleMap.MAP_TYPE_HYBRID) {
                map!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
        } else {
            if (map!!.mapType != GoogleMap.MAP_TYPE_NORMAL) {
                map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }

    private fun onLocationFound(location: Location) {
        if (centerOnMapLoad != null) {
            centerOnMapLoad = null
            return
        }

        val latLng = LatLng(location.latitude, location.longitude)
        val cameraPosition = CameraPosition.builder()
                .target(latLng)
                .zoom(DEFAULT_ZOOM)
                .tilt(DEFAULT_TILT)
                .build()
        map!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        if (on<NightDayHandler>().isNight(Date(), location)) {
            map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(on<ApplicationHandler>().app, closer.vlllage.com.closer.R.raw.google_maps_night_mode))
        }

        on<RefreshHandler>().refreshGroupActions(LatLng(location.latitude, location.longitude))
    }

    fun centerOn(mapBubbles: Collection<MapBubble>) {
        if (mapBubbles.isEmpty()) {
            return
        }

        val builder = LatLngBounds.Builder()
        for (mapBubble in mapBubbles) {
            builder.include(mapBubble.latLng!!)
        }

        try {
            val cu = CameraUpdateFactory.newLatLngBounds(builder.build(), Math.min(
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

    fun onMapIdleObservable() = onMapIdleObservable.observeOn(AndroidSchedulers.mainThread())!!
    fun onMapReadyObservable() = onMapReadyObservable.observeOn(AndroidSchedulers.mainThread())!!

    companion object {
        const val DEFAULT_ZOOM = 18f
        const val DEFAULT_TILT = 45f
    }
}
