package closer.vlllage.com.closer.handler.data

import at.bluesource.choicesdk.maps.common.LatLng
import closer.vlllage.com.closer.api.models.*
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.Property
import java.util.*
import kotlin.collections.HashSet

class RefreshHandler constructor(private val on: On) {

    private val connectionError: (Throwable) -> Unit = {
        on<ConnectionErrorHandler>().notifyConnectionError()
    }

    fun refreshAll() {
        refreshMe()
        refreshMyGroups()
        refreshMyMessages()
        refreshMyEvents()
        refreshActiveQuestProgresses()
    }

    fun refreshMe(callback: ((Phone) -> Unit)? = null) {
        on<DisposableHandler>().add(on<ApiHandler>().phone().subscribe({
            val phone = on<ApiModelHandler>().from(it)
            refresh(phone)
            callback?.invoke(phone)
        }, connectionError))
    }

    fun refreshMyMessages(groupId: String? = null) {
        groupId?.let {
            on<DisposableHandler>().add(on<ApiHandler>().getGroupMessages(it).subscribe({ groupMessages ->
                this.handleMessages(groupMessages)
            }, connectionError))
        }

        on<LocationHandler>().getCurrentLocation { location ->
            on<DisposableHandler>().add(on<ApiHandler>().myMessages(
                LatLng(
                    location.latitude,
                    location.longitude
            )
            ).subscribe({ this.handleMessages(it) }, connectionError))
        }
    }

    fun refreshMyGroups() {
        on<LocationHandler>().getCurrentLocation { location ->
            on<DisposableHandler>().add(on<ApiHandler>().myGroups(LatLng(
                    location.latitude,
                    location.longitude
            )).subscribe({ stateResult ->
                handleGroups(stateResult.groups!!, deleteLocal = true)
                handleFullListResult(stateResult.groupInvites, GroupInvite::class.java, GroupInvite_.id, true, { GroupInviteResult.from(it) })
                handleGroupContacts(stateResult.groupContacts!!, noGroups = true)
            }, connectionError))
        }
    }

    fun refreshMyEvents() {
        on<DisposableHandler>().add(on<ApiHandler>().myEvents().subscribe({ events ->
            handleFullListResult(events, Event::class.java, Event_.id, false, { EventResult.from(it) })
        }, connectionError))
    }

    fun refreshDirectGroups() {
        on<ApiHandler>().getDirectGroups().subscribe({ groups ->
            handleGroups(groups)
        }, connectionError).also {
            on<DisposableHandler>().add(it)
        }
    }

    fun refreshGroup(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroup(groupId).subscribe({
            handleGroups(listOf(it))
        }, connectionError))
    }

    fun refreshGroupForPhone(phoneId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupForPhone(phoneId).subscribe({
            handleGroups(listOf(it))
        }, connectionError))
    }

    fun refreshGroupContactsForPhone(phoneId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupContactsForPhone(phoneId).subscribe({
            handleGroupContacts(it!!, noPhones = true, removeAllExcept = false)
        }, connectionError))
    }

    fun refreshGroupContacts(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getContacts(groupId).subscribe({ handleGroupContacts(it, noGroups = true, removeAllExcept = false) },
                connectionError))
    }

    fun refreshEvents(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getEvents(latLng).subscribe({ eventResults -> handleFullListResult(eventResults, Event::class.java, Event_.id, false, { EventResult.from(it) }) }, connectionError))
    }

    fun refreshPhysicalGroups(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getPhysicalGroups(latLng).subscribe({ groupResults -> handleFullListResult(groupResults, Group::class.java, Group_.id, false, { GroupResult.from(it) }) }, connectionError))

        on<DisposableHandler>().add(on<ApiHandler>().myMessages(latLng)
                .subscribe({ this.handleMessages(it) }, connectionError))
    }

    fun refreshQuests(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuests(latLng).subscribe({ questResults -> handleFullListResult(questResults, Quest::class.java, Quest_.id, false, { QuestResult.from(it) }) }, connectionError))
    }

    fun refreshQuest(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuest(questId).subscribe({ questResult -> handleFullListResult(listOf(questResult), Quest::class.java, Quest_.id, false, { QuestResult.from(it) }) }, connectionError))
    }

    fun refreshActiveQuestProgresses() {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgresses().subscribe({ questProgressResults -> handleFullListResult(questProgressResults, QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }) }, connectionError))
    }

    fun refreshQuestActions(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestActions(questId).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }) }, connectionError))
    }

    fun refreshQuestProgresses(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgresses(questId).subscribe({ questProgressResults -> handleFullListResult(questProgressResults, QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }) }, connectionError))
    }

    fun refreshQuestProgress(questProgressId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgress(questProgressId).subscribe({ questProgressResult -> handleFullListResult(listOf(questProgressResult), QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }) }, connectionError))
    }

    fun refreshGroupActions(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupActions(groupId).subscribe({ groupActionResults ->
            val removeQuery = on<StoreHandler>().store.box(GroupAction::class).query()
                    .equal(GroupAction_.group, groupId)

            for (groupActionResult in groupActionResults) {
                removeQuery.notEqual(GroupAction_.id, groupActionResult.id!!)
            }

            val removeIds = removeQuery.build().findIds()
            on<StoreHandler>().store.box(GroupAction::class).remove(*removeIds)

            handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) })
        }, connectionError))
    }

    fun refreshGroupActions(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupActions(latLng).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }) }, connectionError))
    }

    fun refreshPins(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getPins(groupId).subscribe({ pinResults ->
            val groupMessageResults = mutableListOf<GroupMessageResult>()

            for (pinResult in pinResults) {
                groupMessageResults.add(pinResult.message!!)
            }

            handleMessages(groupMessageResults)

            on<StoreHandler>().store.box(Pin::class).query().equal(Pin_.to, groupId).build().remove()
            handleFullListResult(pinResults, Pin::class.java, Pin_.id, false, { PinResult.from(it) })
        }, connectionError))
    }

    fun refreshPhone(phoneId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getPhone(phoneId)
                .subscribe({ this.refresh(on<ApiModelHandler>().from(it)) }, connectionError))
    }

    fun refreshGroupMessages(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupMessages(groupId)
                .subscribe({ this.handleMessages(it) }, connectionError))
    }

    fun refreshGroupMessagesForPhone(phoneId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getMessagesForPhone(phoneId)
                .subscribe({ this.handleMessages(it) }, connectionError))
    }

    fun refreshGroupMessage(groupMessageId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupMessage(groupMessageId)
                .subscribe({ groupMessageResult -> refresh(GroupMessageResult.from(groupMessageResult)) },
                        connectionError))
    }

    fun refresh(baseObject: BaseObject) {
        when (baseObject) {
            is Event -> refreshObject(baseObject, Event::class.java)
            is Group -> refreshObject(baseObject, Group::class.java)
            is Phone -> refreshObject(baseObject, Phone::class.java)
            is Quest -> refreshObject(baseObject, Quest::class.java)
            is QuestProgress -> refreshObject(baseObject, QuestProgress::class.java)
            is GroupMessage -> refreshObject(baseObject, GroupMessage::class.java)
            is GroupMember -> refreshObject(baseObject, GroupMember::class.java)
        }
    }

    private fun <T : BaseObject> refreshObject(obj: T, clazz: Class<T>) {
        on<StoreHandler>().store.box(clazz).put(obj)
    }

    private fun handleMessages(messages: List<GroupMessageResult>) {
        val phoneResults = mutableListOf<PhoneResult>()
        for (groupMessageResult in messages) {
            if (groupMessageResult.phone != null) {
                phoneResults.add(groupMessageResult.phone!!)
            }
        }

        handlePhones(phoneResults)

        val query = on<StoreHandler>().store.box(GroupMessage::class).query()

        var isFirst = true
        for (message in messages) {
            if (isFirst) {
                isFirst = false
            } else {
                query.or()
            }

            query.equal(GroupMessage_.id, message.id!!)
        }

        query.build().subscribe().single()
                .observer { groupMessages ->
                    val existingObjsMap = HashMap<String, GroupMessage>()
                    for (existingObj in groupMessages) {
                        existingObjsMap[existingObj.id!!] = existingObj
                    }

                    val groupMessageBox = on<StoreHandler>().store.box(GroupMessage::class)

                    for (message in messages) {
                        existingObjsMap[message.id]?.let { existing ->
                            if (message.reactions != existing.reactions ||
                                    existing.text != message.text ||
                                    existing.attachment != message.attachment ||
                                    existing.replies != message.replies ||
                                    existing.created != message.created ||
                                    existing.updated != message.updated
                            ) {
                                existing.reactions = message.reactions
                                existing.created = message.created
                                existing.updated = message.updated
                                existing.text = message.text
                                existing.attachment = message.attachment
                                existing.replies = message.replies
                                groupMessageBox.put(existing)
                            }
                        } ?: run {
                            groupMessageBox.put(GroupMessageResult.from(message))
                        }
                    }
                }
    }

    internal fun <T : BaseObject, R : ModelResult> handleFullListResult(
            results: List<R>?,
            clazz: Class<T>,
            idProperty: Property<T>,
            deleteLocalNotReturnedFromServer: Boolean,
            createTransformer: (R) -> T) {
        results ?: return

        if (deleteLocalNotReturnedFromServer) {
            on<StoreHandler>().removeAllExcept(clazz, idProperty, results.map { it.id!! }.toSet(), false)
        }

        on<StoreHandler>().store.box(clazz).put(results.map { createTransformer.invoke(it) })
    }

    fun handleLifestylesAndGoals(phoneResult: PhoneResult) {
        handleFullListResult(phoneResult.goals, Goal::class.java, Goal_.id, false, { on<ApiModelHandler>().from(it) })
        handleFullListResult(phoneResult.lifestyles, Lifestyle::class.java, Lifestyle_.id, false, { on<ApiModelHandler>().from(it) })
    }

    private fun handleGroups(groups: List<GroupResult>, deleteLocal: Boolean = false) {
        handleFullListResult(groups, Group::class.java, Group_.id, deleteLocal, { GroupResult.from(it) })
    }

    fun handleGroupContacts(groupContacts: List<GroupContactResult>, noGroups: Boolean = false, noPhones: Boolean = false, removeAllExcept: Boolean = true) {
        val allMyGroupContactIds = HashSet<String>()

        if (!noPhones) {
            val phoneResults = mutableListOf<PhoneResult>()
            for (groupContactResult in groupContacts) {
                allMyGroupContactIds.add(groupContactResult.id!!)

                if (groupContactResult.phone != null) {
                    phoneResults.add(groupContactResult.phone!!)
                }
            }

            handlePhones(phoneResults)
        }

        if (!noGroups) {
            val groupResults = mutableListOf<GroupResult>()
            for (groupContactResult in groupContacts) {
                allMyGroupContactIds.add(groupContactResult.id!!)

                if (groupContactResult.group != null) {
                    groupResults.add(groupContactResult.group!!)
                }
            }

            handleGroups(groupResults)
        }

        on<StoreHandler>().store.box(GroupContact::class).put(groupContacts.map { GroupContactResult.from(it) })

        if (removeAllExcept) {
            on<StoreHandler>().removeAllExcept(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds)
        }
    }

    private fun handlePhones(phoneResults: List<PhoneResult>) {
        handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false, { on<ApiModelHandler>().from(it) })
    }
}
