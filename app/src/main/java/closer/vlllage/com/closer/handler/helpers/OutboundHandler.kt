package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.net.Uri
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.model.LatLng

class OutboundHandler : PoolMember() {
    fun openDirections(latLng: LatLng?) {
        if (latLng == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latLng.latitude + "," + latLng.longitude))
        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }
}
