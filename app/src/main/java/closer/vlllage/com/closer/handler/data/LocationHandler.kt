package closer.vlllage.com.closer.handler.data

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.queatz.on.On
import com.queatz.on.OnLifecycle

class LocationHandler constructor(private val on: On) {

    private var fusedLocationProvider: FusedLocationProviderClient = if (on<ActivityHandler>().isPresent) {
        LocationServices.getFusedLocationProviderClient(
                on<ActivityHandler>().activity!!
        )
    } else {
        LocationServices.getFusedLocationProviderClient(
                on<ApplicationHandler>().app
        )
    }

    var lastKnownLocation: Location? = null
        private set

    fun getCurrentLocation(callback: (Location) -> Unit) {
        getCurrentLocation(callback, null)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (Location) -> Unit, locationUnavailableCallback: (() -> Unit)?) {
        on<PermissionHandler>()
                .check(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .`when` { granted ->
                    if (granted) {
                        fusedLocationProvider.lastLocation.addOnCompleteListener { task ->
                            if (!task.isSuccessful || task.result == null) {
                                waitForLocation(callback, locationUnavailableCallback)
                                return@addOnCompleteListener
                            }

                            lastKnownLocation = task.result

                            callback.invoke(task.result!!)
                        }
                    } else locationUnavailableCallback?.invoke()
                }
    }

    @SuppressLint("MissingPermission")
    private fun waitForLocation(callback: (Location) -> Unit, locationUnavailableCallback: (() -> Unit)?) {
        val locationRequest = LocationRequest.create()
                .setExpirationDuration(10000)
                .setNumUpdates(1)

        fusedLocationProvider.locationAvailability.addOnCompleteListener { task ->
            if (task.isSuccessful && !task.result!!.isLocationAvailable) {
                locationUnavailableCallback?.invoke()
            } else {
                fusedLocationProvider.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult!!.lastLocation == null) {
                            locationUnavailableCallback?.invoke()

                            return
                        }

                        lastKnownLocation = locationResult.lastLocation
                        callback.invoke(locationResult.lastLocation)
                    }
                }, on<ActivityHandler>().activity!!.mainLooper)
            }
        }
    }
}
