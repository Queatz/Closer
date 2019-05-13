package closer.vlllage.com.closer.handler.data

import com.google.android.gms.maps.model.LatLng

import java.util.ArrayList

import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Event_
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import io.reactivex.Single

class DataHandler : PoolMember() {
    fun getPhonesNear(latLng: LatLng): Single<List<Phone>> {
        return `$`(ApiHandler::class.java).getPhonesNear(latLng)
                .doOnSuccess { phoneResults -> `$`(RefreshHandler::class.java).handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false,
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

    fun getEventById(eventId: String): Single<Event> {
        val event = `$`(StoreHandler::class.java).store.box(Event::class.java).query()
                .equal(Event_.id, eventId)
                .build().findFirst()

        return if (event != null) {
            Single.just(event)
        } else `$`(ApiHandler::class.java).getEvent(eventId).map<Event>({ EventResult.from(it) })
                .doOnSuccess { eventFromServer -> `$`(RefreshHandler::class.java).refresh(eventFromServer) }

    }

    fun getPhone(phoneId: String): Single<Phone> {
        val phone = `$`(StoreHandler::class.java).store.box(Phone::class.java).query()
                .equal(Phone_.id, phoneId)
                .build().findFirst()

        return if (phone != null) {
            Single.just(phone)
        } else `$`(ApiHandler::class.java).getPhone(phoneId).map<Phone>({ PhoneResult.from(it) })
                .doOnSuccess { phoneFromServer -> `$`(RefreshHandler::class.java).refresh(phoneFromServer) }

    }

    fun getGroupForPhone(phoneId: String): Single<Group> {
        val group = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                .equal(Group_.phoneId, phoneId)
                .build().findFirst()

        return if (group != null) {
            Single.just(group)
        } else `$`(ApiHandler::class.java).getGroupForPhone(phoneId).map<Group>({ GroupResult.from(it) })
                .doOnSuccess { groupFromServer -> `$`(RefreshHandler::class.java).refresh(groupFromServer) }

    }
}
