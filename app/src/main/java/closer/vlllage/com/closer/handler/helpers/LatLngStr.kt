package closer.vlllage.com.closer.handler.helpers

import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On

class LatLngStr constructor(private val on: On) {

    fun from(latLng: LatLng): String {
        return latLng.latitude.toString() + "," + latLng.longitude
    }

    fun to(latLng: String): LatLng {
        val parts = latLng.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return LatLng(java.lang.Double.valueOf(parts[0]), java.lang.Double.valueOf(parts[1]))
    }

    fun to(latLng: List<Double>?): LatLng? {
        return if (latLng == null) {
            null
        } else LatLng(latLng[0], latLng[1])

    }

    fun to(latitude: Double, longitude: Double): LatLng {
        return LatLng(latitude, longitude)
    }
}
