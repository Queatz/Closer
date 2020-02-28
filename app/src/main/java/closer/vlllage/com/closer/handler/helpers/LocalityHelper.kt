package closer.vlllage.com.closer.handler.helpers

import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.Observable

class LocalityHelper constructor(private val on: On) {

    private val geocoder = Geocoder(on<ApplicationHandler>().app)

    fun getLocality(latLng: LatLng, callback: (String?) -> Unit) {
        on<DisposableHandler>().add(Observable.fromCallable {
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
        }.subscribe({ addresses ->
            callback.invoke(addresses?.firstOrNull { it.locality != null }?.locality)
        }) { })
    }
}
