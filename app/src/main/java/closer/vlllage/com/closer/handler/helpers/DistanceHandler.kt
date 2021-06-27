package closer.vlllage.com.closer.handler.helpers

import android.location.Location.distanceBetween
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On

class DistanceHandler constructor(private val on: On) {
    fun isPhoneNearGroup(group: Group, phone: Phone? = null): Boolean {
        if (!group.physical || group.latitude == null || group.longitude == null) {
            return false
        }

        val results = FloatArray(1)
        val location: LatLng = phone?.let {
            if (it.latitude != null && phone.longitude != null) {
                LatLng(
                        phone.latitude!!,
                        phone.longitude!!
                )
            } else {
                return false
            }
        } ?: on<LocationHandler>().lastKnownLocation?.let {
            LatLng(
                    it.latitude,
                    it.longitude
            )
        } ?: return false

        distanceBetween(group.latitude!!, group.longitude!!, location.latitude, location.longitude, results)

        return results[0] < 402
    }
}
