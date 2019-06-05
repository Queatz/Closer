package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.Single
import java.util.*

class DataHandler constructor(private val on: On) {
    fun getPhonesNear(latLng: LatLng): Single<List<Phone>> {
        return on<ApiHandler>().getPhonesNear(latLng)
                .doOnSuccess { phoneResults -> on<RefreshHandler>().handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false,
                        { PhoneResult.from(it) },
                        { phone, phoneResult -> PhoneResult.updateFrom(phone, phoneResult) }) }
                .map { phoneResults ->
                    val result = ArrayList<Phone>(phoneResults.size)
                    for (phoneResult in phoneResults) {
                        result.add(PhoneResult.from(phoneResult))
                    }
                    result
                }
    }

    fun getGroupById(groupId: String): Single<Group> {
        val group = on<StoreHandler>().store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build().findFirst()

        return if (group != null) {
            Single.just(group)
        } else on<ApiHandler>().getGroup(groupId)
                .map { GroupResult.from(it) }
                .doOnSuccess { eventFromServer -> on<RefreshHandler>().refresh(eventFromServer) }
    }

    fun getEventById(eventId: String): Single<Event> {
        val event = on<StoreHandler>().store.box(Event::class.java).query()
                .equal(Event_.id, eventId)
                .build().findFirst()

        return if (event != null) {
            Single.just(event)
        } else on<ApiHandler>().getEvent(eventId).map<Event> { EventResult.from(it) }
                .doOnSuccess { eventFromServer -> on<RefreshHandler>().refresh(eventFromServer) }

    }

    fun getPhone(phoneId: String): Single<Phone> {
        val phone = on<StoreHandler>().store.box(Phone::class.java).query()
                .equal(Phone_.id, phoneId)
                .build().findFirst()

        return if (phone != null) {
            Single.just(phone)
        } else on<ApiHandler>().getPhone(phoneId).map<Phone> { PhoneResult.from(it) }
                .doOnSuccess { phoneFromServer -> on<RefreshHandler>().refresh(phoneFromServer) }

    }

    fun getGroupForPhone(phoneId: String): Single<Group> {
        val group = on<StoreHandler>().store.box(Group::class.java).query()
                .equal(Group_.phoneId, phoneId)
                .build().findFirst()

        return if (group != null) {
            Single.just(group)
        } else on<ApiHandler>().getGroupForPhone(phoneId).map<Group>({ GroupResult.from(it) })
                .doOnSuccess { groupFromServer -> on<RefreshHandler>().refresh(groupFromServer) }

    }
}
