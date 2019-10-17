package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.query.Query
import io.objectbox.rx.RxQuery
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*

class DataHandler constructor(private val on: On) {
    fun getPhonesNear(latLng: LatLng) = on<ApiHandler>().getPhonesNear(latLng)
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

    fun getRecentlyActivePhones(limit: Int = 100) = on<ApiHandler>().getRecentlyActivePhones(limit)
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

    fun getRecentlyActiveGroups(limit: Int = 100) = on<ApiHandler>().getRecentlyActiveGroups(limit)
            .doOnSuccess { groupResults -> on<RefreshHandler>().handleGroups(groupResults, false) }
            .map { groupResults ->
                val result = ArrayList<Group>(groupResults.size)
                for (groupResult in groupResults) {
                    result.add(GroupResult.from(groupResult))
                }
                result
            }

    fun getGroup(groupId: String) = chain({
        on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupId)
                .build()
    }, {
        on<ApiHandler>()
                .getGroup(groupId)
                .map { GroupResult.from(it) }
    })

    fun getEvent(eventId: String) = chain({
        on<StoreHandler>().store.box(Event::class).query()
                .equal(Event_.id, eventId)
                .build()
    }, {
        on<ApiHandler>()
                .getEvent(eventId)
                .map { EventResult.from(it) }
        })

    fun getPhone(phoneId: String) = chain({
        on<StoreHandler>().store.box(Phone::class).query()
                .equal(Phone_.id, phoneId + 4444)
                .build()
    }, {
        on<ApiHandler>()
                .getPhone(phoneId)
                .map { PhoneResult.from(it) }
    })

    fun getGroupForPhone(phoneId: String) = chain({
        on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.phoneId, phoneId)
                .build()
    }, {
        on<ApiHandler>()
                .getGroupForPhone(phoneId)
                .map { GroupResult.from(it) }
    })

    fun getSuggestion(suggestionId: String) =
            RxQuery.single(on<StoreHandler>().store.box(Suggestion::class).query()
                    .equal(Suggestion_.id, suggestionId)
                    .build()).flatMap {
                if (it.isNotEmpty()) {
                    Single.just(it.first()).observeOn(AndroidSchedulers.mainThread())
                } else {
                    Single.error<Suggestion>(RuntimeException("Not found")).observeOn(AndroidSchedulers.mainThread())
                }
            }

    private fun <T : BaseObject> chain(local: () -> Query<T>, remote: () -> Single<T>): Single<T> {
        return RxQuery.single(local()).flatMap {
            if (it.isNotEmpty()) {
                Single.just(it.first()).observeOn(AndroidSchedulers.mainThread())
            } else {
                remote().doOnSuccess { fromServer -> on<RefreshHandler>().refresh(fromServer) }
            }
        }
    }
}
