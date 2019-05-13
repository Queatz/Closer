package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.model.LatLng

class LatLngStr : PoolMember() {

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

    fun to(latitude: Double?, longitude: Double?): LatLng {
        return LatLng(latitude!!, longitude!!)
    }
}
