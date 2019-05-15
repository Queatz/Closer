package closer.vlllage.com.closer.handler.helpers

import android.location.Location.distanceBetween
import closer.vlllage.com.closer.handler.data.LocationHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.models.Group

class DistanceHandler constructor(private val on: On) {
    fun isUserNearGroup(group: Group): Boolean {
        if (!group.physical) {
            return false
        }

        val results = FloatArray(1)
        val location = on<LocationHandler>().lastKnownLocation ?: return false

        distanceBetween(group.latitude!!, group.longitude!!, location.latitude, location.longitude, results)

        return results[0] < 402
    }
}
