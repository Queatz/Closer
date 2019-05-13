package closer.vlllage.com.closer.handler.helpers

import android.location.Location.distanceBetween
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Group

class DistanceHandler : PoolMember() {
    fun isUserNearGroup(group: Group): Boolean {
        if (!group.physical) {
            return false
        }

        val results = FloatArray(1)
        val location = `$`(LocationHandler::class.java).lastKnownLocation ?: return false

        distanceBetween(group.latitude!!, group.longitude!!, location.latitude, location.longitude, results)

        return results[0] < 402
    }
}
