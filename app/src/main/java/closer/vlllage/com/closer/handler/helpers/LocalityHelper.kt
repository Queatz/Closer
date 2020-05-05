package closer.vlllage.com.closer.handler.helpers

import android.location.Address
import android.location.Geocoder
import closer.vlllage.com.closer.handler.map.PlacesHandler
import closer.vlllage.com.closer.handler.map.SuggestionHandler
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

class LocalityHelper constructor(private val on: On) {

    private val geocoder = Geocoder(on<ApplicationHandler>().app)

    fun getName(latLng: LatLng, callback: (String?) -> Unit) {
        on<DisposableHandler>().add(on<PlacesHandler>().reverseGeocode(latLng).subscribe({
            callback.invoke(it.firstOrNull { it.properties.name != null || (it.properties.houseNumber != null && it.properties.street != null) }?.properties?.let { place ->
                place.name ?: "${place.houseNumber} ${place.street}"
            })
        }, {
            callback.invoke("Error")
        }))
    }

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
