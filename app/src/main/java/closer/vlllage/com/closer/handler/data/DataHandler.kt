package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On
import io.objectbox.query.Query
import io.objectbox.rx.RxQuery
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

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

    fun getDirectGroup(phoneId: String) = chain({
        on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, "i am not an id") // todo always fail for now
                .build()
    }, {
        on<ApiHandler>()
                .getDirectGroup(phoneId)
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

    fun getGroupMember(groupId: String) = chain({
        on<StoreHandler>().store.box(GroupMember::class).query()
                .equal(GroupMember_.group, groupId)
                .equal(GroupMember_.phone, on<PersistenceHandler>().phoneId!!)
                .build()
    }, {
        on<ApiHandler>().getGroupMember(groupId)
                .map { GroupMemberResult.from(it) }
    })

    fun getQuest(questId: String) = chain({
        on<StoreHandler>().store.box(Quest::class).query()
                .equal(Quest_.id, questId)
                .notNull(Quest_.groupId)
                .build()
    }, {
        on<ApiHandler>()
                .getQuest(questId)
                .map { QuestResult.from(it) }
    })

    fun getQuestProgress(questProgressId: String) = chain({
        on<StoreHandler>().store.box(QuestProgress::class).query()
                .equal(QuestProgress_.id, questProgressId)
                .notNull(QuestProgress_.groupId)
                .build()
    }, {
        on<ApiHandler>()
                .getQuestProgress(questProgressId)
                .map { QuestProgressResult.from(it) }
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
                .equal(Phone_.id, phoneId)
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



    fun getStory(storyId: String) = chain({
        on<StoreHandler>().store.box(Story::class).query()
                .equal(Story_.id, storyId)
                .build()
    }, {
        on<ApiHandler>().getStory(storyId)
                .map { StoryResult.from(on, it) }
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

    fun getGroupContact(groupId: String, phoneId: String) = chain({
        on<StoreHandler>().store.box(GroupContact::class).query()
                .equal(GroupContact_.groupId, groupId)
                .equal(GroupContact_.contactId, phoneId)
                .build()
    }, {
        Single.error(Throwable("Not found"))
    })

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
