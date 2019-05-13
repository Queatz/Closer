package closer.vlllage.com.closer.handler.data

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationHandler : PoolMember() {

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    var lastKnownLocation: Location? = null
        private set

    override fun onPoolInit() {
        fusedLocationProvider = if (`$`(ActivityHandler::class.java).isPresent) {
            LocationServices.getFusedLocationProviderClient(
                    `$`(ActivityHandler::class.java).activity!!
            )
        } else {
            LocationServices.getFusedLocationProviderClient(
                    `$`(ApplicationHandler::class.java).app
            )
        }
    }

    fun getCurrentLocation(callback: (Location) -> Unit) {
        getCurrentLocation(callback, null)
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(callback: (Location) -> Unit, locationUnavailableCallback: (() -> Unit)?) {
        `$`(PermissionHandler::class.java)
                .check(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .`when` { granted ->
                    if (granted) {
                        fusedLocationProvider!!.lastLocation.addOnCompleteListener { task ->
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

        fusedLocationProvider!!.locationAvailability.addOnCompleteListener { task ->
            if (task.isSuccessful && !task.result!!.isLocationAvailable) {
                locationUnavailableCallback?.invoke()
            } else {
                fusedLocationProvider!!.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult!!.lastLocation == null) {
                            locationUnavailableCallback?.invoke()

                            return
                        }

                        lastKnownLocation = locationResult.lastLocation
                        callback.invoke(locationResult.lastLocation)
                    }
                }, `$`(ActivityHandler::class.java).activity!!.mainLooper)
            }
        }
    }
}
