package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.ApiModelHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.models.Phone
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On
import io.reactivex.rxjava3.subjects.BehaviorSubject

class MeetHandler constructor(private val on: On) {

    companion object {
        private val phones = BehaviorSubject.createDefault(mutableListOf<PhoneResult>())
        private val total = BehaviorSubject.createDefault(0)
    }

    val phones = MeetHandler.phones
    val total = MeetHandler.total

    fun meet(phoneId: String, meet: Boolean) {
        on<ApplicationHandler>().app.on<DisposableHandler>().add(
                on<ApiHandler>().setMeet(phoneId, meet).subscribe({}, {
                    on<DefaultAlerts>().thatDidntWork()
                })
        )
    }

    fun setLocation(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getNewPhonesToMeetNear(latLng)
                .subscribe({
                    total.onNext(it.total)
                    phones.onNext(it.phones.toMutableList())
                }, {
                    on<ConnectionErrorHandler>().notifyConnectionError()
                }))
    }

    fun next() = nextPhone()?.let {
        on<NavigationHandler>().showProfile(it.id!!, meet = true, close = true)
        true
    } ?: false

    private fun nextPhone(): Phone? {
        return phones.value!!.firstOrNull()?.let {
            phones.value!!.removeAt(0)
            total.onNext(phones.value!!.size)
            on<ApiModelHandler>().from(it)
        }
    }
}
