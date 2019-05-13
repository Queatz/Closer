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
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class MapHandler : PoolMember(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var mapView: View? = null
    private var centerOnMapLoad: LatLng? = null
    private var onMapChangedListener: OnMapChangedListener? = null
    private var onMapClickedListener: OnMapClickedListener? = null
    private var onMapLongClickedListener: OnMapLongClickedListener? = null
    private var onMapReadyListener: OnMapReadyListener? = null
    private var onMapIdleListener: OnMapIdleListener? = null

    private val onMapIdleObservable = BehaviorSubject.create<CameraPosition>()

    val center: LatLng?
        get() = if (map == null) {
            null
        } else map!!.cameraPosition.target

    val zoom: Float
        get() = if (map == null) {
            0f
        } else map!!.cameraPosition.zoom

    val visibleRegion: VisibleRegion?
        get() = if (map == null) {
            null
        } else map!!.projection.visibleRegion

    fun attach(mapFragment: MapFragment) {
        mapFragment.getMapAsync(this)
        mapView = mapFragment.view
    }

    fun setOnMapChangedListener(onMapChangedListener: OnMapChangedListener) {
        this.onMapChangedListener = onMapChangedListener
    }

    fun setOnMapClickedListener(onMapClickedListener: OnMapClickedListener) {
        this.onMapClickedListener = onMapClickedListener
    }

    fun setOnMapLongClickedListener(onMapLongClickedListener: OnMapLongClickedListener) {
        this.onMapLongClickedListener = onMapLongClickedListener
    }

    fun setOnMapReadyListener(onMapReadyListener: OnMapReadyListener) {
        this.onMapReadyListener = onMapReadyListener
    }

    fun setOnMapIdleListener(onMapIdleListener: OnMapIdleListener) {
        this.onMapIdleListener = onMapIdleListener
    }

    fun centerMap(latLng: LatLng) {
        centerOnMapLoad = latLng

        if (map != null) {
            map!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (`$`(PersistenceHandler::class.java).lastMapCenter != null) {
            map!!.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(
                    `$`(PersistenceHandler::class.java).lastMapCenter!!,
                    DEFAULT_ZOOM
            )))
        }

        onMapReadyListener!!.onMapReady(map!!)
        map!!.setOnCameraMoveListener { this.mapChanged() }
        map!!.setOnMapClickListener { onMapClickedListener!!.onMapClicked(it) }
        map!!.setOnMapLongClickListener { onMapLongClickedListener!!.onMapLongClicked(it) }
        map!!.setOnCameraIdleListener {
            `$`(PersistenceHandler::class.java).lastMapCenter = map!!.cameraPosition.target
            onMapIdleListener!!.onMapIdle(map!!.cameraPosition.target)
            onMapIdleObservable.onNext(map!!.cameraPosition)
        }
        mapView!!.addOnLayoutChangeListener { v, i1, i2, i3, i4, i5, i6, i7, i8 -> onMapChangedListener!!.onMapChanged() }
        onMapChangedListener!!.onMapChanged()
        map!!.setPadding(0, `$`(WindowHandler::class.java).statusBarHeight, 0, 0)

        if (centerOnMapLoad == null) {
            locateMe()
        } else {
            map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOnMapLoad, DEFAULT_ZOOM))
            centerOnMapLoad = null
        }

        updateMyLocationEnabled()
    }

    fun locateMe() {
        `$`(LocationHandler::class.java).getCurrentLocation { location: Location -> this.onLocationFound(location) }
    }

    @SuppressLint("MissingPermission")
    fun updateMyLocationEnabled() {
        if (map == null) {
            return
        }

        `$`(PermissionHandler::class.java)
                .check(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .`when` { granted -> map!!.isMyLocationEnabled = granted }
    }

    private fun mapChanged() {
        onMapChangedListener!!.onMapChanged()

        if (map == null) {
            return
        }

        if (map!!.cameraPosition.zoom >= 18) {
            if (map!!.mapType != GoogleMap.MAP_TYPE_SATELLITE) {
                map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
            }
        } else if (map!!.cameraPosition.zoom <= 3) {
            if (map!!.mapType != GoogleMap.MAP_TYPE_SATELLITE) {
                map!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
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

        if (`$`(NightDayHandler::class.java).isNight(Date(), location)) {
            map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(`$`(ApplicationHandler::class.java).app, R.raw.google_maps_night_mode))
        }

        `$`(RefreshHandler::class.java).refreshGroupActions(LatLng(location.latitude, location.longitude))
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
                    `$`(ActivityHandler::class.java).activity!!.window.decorView.width,
                    `$`(ActivityHandler::class.java).activity!!.window.decorView.height
            ) / 4)
            map!!.animateCamera(cu, 225, null)
        } catch (ignored: Throwable) {
            // com.google.maps.api.android.lib6.common.apiexception.c:
            // Error using newLatLngBounds(LatLngBounds, int):
            // View size is too small after padding is applied.
        }

    }

    fun onMapIdleObservable(): Observable<CameraPosition> {
        return onMapIdleObservable
    }

    interface OnMapChangedListener {
        fun onMapChanged()
    }

    interface OnMapReadyListener {
        fun onMapReady(map: GoogleMap)
    }

    interface OnMapClickedListener {
        fun onMapClicked(latLng: LatLng)
    }

    interface OnMapLongClickedListener {
        fun onMapLongClicked(latLng: LatLng)
    }

    interface OnMapIdleListener {
        fun onMapIdle(latLng: LatLng)
    }

    companion object {

        private val DEFAULT_ZOOM = 15f
        private val DEFAULT_TILT = 15f
    }
}
