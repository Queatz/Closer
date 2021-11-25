package closer.vlllage.com.closer.handler.map

import android.location.Address
import android.location.Geocoder
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*


class PlacesHandler constructor(private val on: On) {

    private val geocoder = Geocoder(on<ActivityHandler>().activity, Locale.getDefault())

    fun findAddress(address: String, limit: Int = 10): Single<List<Address>> =
        Single.fromCallable { geocoder.getFromLocationName(address, limit) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    fun findPlace(query: String, latLng: LatLng? = null) = on<ApiHandler>().getPlaces(query, latLng ?: on<MapHandler>().center!!).map {
        it.features
    }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun reverseGeocode(latLng: LatLng) = on<ApiHandler>().reverseGeocode(latLng).map {
        it.features
    }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
