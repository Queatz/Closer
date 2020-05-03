package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.*
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
                    { on<ApiModelHandler>().from(it) },
                    { phone, phoneResult -> on<ApiModelHandler>().updateFrom(phone, phoneResult) }) }
            .map { phoneResults ->
                val result = mutableListOf<Phone>()
                for (phoneResult in phoneResults) {
                    result.add(on<ApiModelHandler>().from(phoneResult))
                }
                result
            }

    fun getRecentlyActivePhones(limit: Int = 100) = on<ApiHandler>().getRecentlyActivePhones(limit)
//            TODO these cause problems
//            .doOnSuccess { phoneResults -> on<RefreshHandler>().handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false,
//                    { on<_root_ide_package_.closer.vlllage.com.closer.handler.data.ApiModelHandler>().from(it) },
//                    { phone, phoneResult -> PhoneResult.updateFrom(phone, phoneResult) }) }
            .map { phoneResults ->
                val result = mutableListOf<Phone>()
                for (phoneResult in phoneResults) {
                    result.add(on<ApiModelHandler>().from(phoneResult))
                }
                result
            }

    fun getRecentlyActiveGroups(limit: Int = 100) = on<ApiHandler>().getRecentlyActiveGroups(limit)
//            TODO these cause problems
//            .doOnSuccess { groupResults -> on<RefreshHandler>().handleGroups(groupResults, false) }
            .map { groupResults -> groupResults.map { GroupResult.from(it) } }

    fun getGroup(groupId: String) = chain({
        on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupId)
                .build()
    }, {
        on<ApiHandler>()
                .getGroup(groupId)
                .map { GroupResult.from(it) }
    })

    fun getGroupMessage(groupMessageId: String) = chain({
        on<StoreHandler>().store.box(GroupMessage::class).query()
                .equal(GroupMessage_.id, groupMessageId)
                .build()
    }, {
        on<ApiHandler>()
                .getGroupMessage(groupMessageId)
                .map { GroupMessageResult.from(it) }
    })

    fun getGroupAction(groupActionId: String) = chain({
        on<StoreHandler>().store.box(GroupAction::class).query()
                .equal(GroupAction_.id, groupActionId)
                .build()
    }, {
        on<ApiHandler>()
                .getGroupAction(groupActionId)
                .map { GroupActionResult.from(it) }
    })

    fun getEvent(eventId: String) = chain({
        on<StoreHandler>().store.box(Event::class).query()
                .equal(Event_.id, eventId)
                .notNull(Event_.groupId)
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
                .map { on<ApiModelHandler>().from(it) }
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
