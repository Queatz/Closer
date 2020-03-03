package closer.vlllage.com.closer.handler.helpers

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class LocalityHelper constructor(private val on: On) {

    private val geocoder = Geocoder(on<ApplicationHandler>().app)

    fun getLocality(latLng: LatLng, callback: (String?) -> Unit) {
        on<DisposableHandler>().add(Observable.fromCallable {
            try {
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5)
            } catch (error: IOException) {
                listOf<Address>()
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ addresses ->
            callback.invoke(addresses?.firstOrNull { it.locality != null }?.locality)
        }) { })
    }
}
