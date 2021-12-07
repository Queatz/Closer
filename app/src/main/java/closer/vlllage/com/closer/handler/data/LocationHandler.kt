package closer.vlllage.com.closer.handler.data

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import at.bluesource.choicesdk.location.common.*
import at.bluesource.choicesdk.location.factory.FusedLocationProviderFactory
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class LocationHandler constructor(private val on: On) {

    private var fusedLocationProvider: FusedLocationProviderClient = if (on<ActivityHandler>().isPresent) {
        FusedLocationProviderFactory.getFusedLocationProviderClient(
                on<ActivityHandler>().activity!!
        )
    } else {
        FusedLocationProviderFactory.getFusedLocationProviderClient(
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
                        fusedLocationProvider.getLastLocation().addOnCompleteListener { task ->
                            if (!task.isSuccessful() || task.getResult() == null) {
                                waitForLocation(callback, locationUnavailableCallback)
                                return@addOnCompleteListener
                            }

                            lastKnownLocation = task.getResult()

                            callback.invoke(task.getResult()!!)
                        }
                    } else locationUnavailableCallback?.invoke()
                }
    }

    @SuppressLint("MissingPermission")
    private fun waitForLocation(callback: (Location) -> Unit, locationUnavailableCallback: (() -> Unit)?) {
        val locationRequest = LocationRequest.Builder()
                .setExpirationDuration(10000)
                .setNumUpdates(1)
                .build()

        fusedLocationProvider.getLocationAvailability().addOnCompleteListener { task ->
            if (task.isSuccessful() && !task.getResult()!!.isLocationAvailable) {
                locationUnavailableCallback?.invoke()
            } else {
                fusedLocationProvider.requestLocationUpdates(locationRequest, object : LocationCallback {
                    override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                        // Ignored
                    }

                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult!!.lastLocation == null) {
                            locationUnavailableCallback?.invoke()

                            return
                        }

                        lastKnownLocation = locationResult.lastLocation
                        callback.invoke(locationResult.lastLocation!!)
                    }
                }, on<ActivityHandler>().activity?.mainLooper ?: Looper.getMainLooper())
            }
        }
    }
}
