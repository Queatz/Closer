package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.net.Uri
import com.queatz.on.On
import com.google.android.gms.maps.model.LatLng

class OutboundHandler constructor(private val on: On) {
    fun openDirections(latLng: LatLng?) {
        if (latLng == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latLng.latitude + "," + latLng.longitude))
        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
