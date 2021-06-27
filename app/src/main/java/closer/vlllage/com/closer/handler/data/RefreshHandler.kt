package closer.vlllage.com.closer.handler.data

import at.bluesource.choicesdk.maps.common.LatLng
import closer.vlllage.com.closer.api.models.*
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.SubscriptionBuilder
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
                handleFullListResult(stateResult.groupInvites, GroupInvite::class.java, GroupInvite_.id, true, { GroupInviteResult.from(it) }, null)
                handleGroupContacts(stateResult.groupContacts!!, noGroups = true)
            }, connectionError))
        }
    }

    fun refreshMyEvents() {
        on<DisposableHandler>().add(on<ApiHandler>().myEvents().subscribe({ events ->
            handleFullListResult(events, Event::class.java, Event_.id, false, { EventResult.from(it) }, { event, eventResult ->  EventResult.updateFrom(event, eventResult) })
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
        on<DisposableHandler>().add(on<ApiHandler>().getEvents(latLng).subscribe({ eventResults -> handleFullListResult(eventResults, Event::class.java, Event_.id, false, { EventResult.from(it) }, { event, eventResult -> EventResult.updateFrom(event, eventResult) }) }, connectionError))
    }

    fun refreshPhysicalGroups(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getPhysicalGroups(latLng).subscribe({ groupResults -> handleFullListResult(groupResults, Group::class.java, Group_.id, false, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) }) }, connectionError))

        on<DisposableHandler>().add(on<ApiHandler>().myMessages(latLng)
                .subscribe({ this.handleMessages(it) }, connectionError))
    }

    fun refreshQuests(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuests(latLng).subscribe({ questResults -> handleFullListResult(questResults, Quest::class.java, Quest_.id, false, { QuestResult.from(it) }, { quest, questResult -> QuestResult.updateFrom(quest, questResult) }) }, connectionError))
    }

    fun refreshQuest(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuest(questId).subscribe({ questResult -> handleFullListResult(listOf(questResult), Quest::class.java, Quest_.id, false, { QuestResult.from(it) }, { quest, questResult -> QuestResult.updateFrom(quest, questResult) }) }, connectionError))
    }

    fun refreshActiveQuestProgresses() {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgresses().subscribe({ questProgressResults -> handleFullListResult(questProgressResults, QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }, { questProgress, questProgressResult -> QuestProgressResult.updateFrom(questProgress, questProgressResult) }) }, connectionError))
    }

    fun refreshQuestActions(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestActions(questId).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) }) }, connectionError))
    }

    fun refreshQuestProgresses(questId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgresses(questId).subscribe({ questProgressResults -> handleFullListResult(questProgressResults, QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }, { questProgress, questProgressResult -> QuestProgressResult.updateFrom(questProgress, questProgressResult) }) }, connectionError))
    }

    fun refreshQuestProgress(questProgressId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getQuestProgress(questProgressId).subscribe({ questProgressResult -> handleFullListResult(listOf(questProgressResult), QuestProgress::class.java, QuestProgress_.id, false, { QuestProgressResult.from(it) }, { questProgress, questProgressResult -> QuestProgressResult.updateFrom(questProgress, questProgressResult) }) }, connectionError))
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

            handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) })
        }, connectionError))
    }

    fun refreshGroupActions(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupActions(latLng).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) }) }, connectionError))
    }

    fun refreshPins(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getPins(groupId).subscribe({ pinResults ->
            val groupMessageResults = mutableListOf<GroupMessageResult>()

            for (pinResult in pinResults) {
                groupMessageResults.add(pinResult.message!!)
            }

            handleMessages(groupMessageResults)

            on<StoreHandler>().store.box(Pin::class).query().equal(Pin_.to, groupId).build().remove()
            handleFullListResult(pinResults, Pin::class.java, Pin_.id, false, { PinResult.from(it) }, { pin, pinResult -> PinResult.updateFrom(pin, pinResult) })
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
            is Event -> refreshObject(baseObject, Event::class.java, Event_.id)
            is Group -> refreshObject(baseObject, Group::class.java, Group_.id)
            is Phone -> refreshObject(baseObject, Phone::class.java, Phone_.id)
            is Quest -> refreshObject(baseObject, Quest::class.java, Quest_.id)
            is QuestProgress -> refreshObject(baseObject, QuestProgress::class.java, QuestProgress_.id)
            is GroupMessage -> refreshObject(baseObject, GroupMessage::class.java, GroupMessage_.id)
            is GroupMember -> refreshObject(baseObject, GroupMember::class.java, GroupMember_.id)
        }
    }

    private fun <T : BaseObject> refreshObject(obj: T, clazz: Class<T>, idProperty: Property<T>) {
        (on<StoreHandler>().store.box(clazz)
                .query()
                .equal(idProperty, obj.id!!)
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread()) as SubscriptionBuilder<List<T>>)
                .observer { results ->
                    if (results.isNotEmpty()) {
                        obj.objectBoxId = results.first().objectBoxId

                        if (obj is Phone) {
                            if (obj.goals == null) {
                                obj.goals = (results.first() as Phone).goals
                            }

                            if (obj.lifestyles == null) {
                                obj.lifestyles = (results.first() as Phone).lifestyles
                            }
                        }
                    }

                    on<StoreHandler>().store.box(clazz).put(obj)
                }
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
            createTransformer: (R) -> T,
            updateTransformer: ((T, R) -> T)?) {
        results ?: return

        val serverIdList = results.map { it.id!! }.toSet()

        on<StoreHandler>().findAll(clazz, idProperty, serverIdList).observer { existingObjs ->
            val existingObjsMap = HashMap<String, T>()
            for (existingObj in existingObjs) {
                existingObjsMap[existingObj.id!!] = existingObj
            }

            val objsToAdd = mutableListOf<T>()
            val idsToAdd = mutableSetOf<String>()

            for (result in results) {
                if (idsToAdd.contains(result.id!!)) continue
                idsToAdd.add(result.id!!)

                if (!existingObjsMap.containsKey(result.id!!)) {
                    objsToAdd.add(createTransformer.invoke(result))
                } else if (updateTransformer != null) {
                    objsToAdd.add(updateTransformer.invoke(existingObjsMap[result.id!!]!!, result))
                }
            }

            on<StoreHandler>().store.tx({
                on<StoreHandler>().store.box(clazz).query()
                        .`in`(idProperty, objsToAdd.map { it.id!! }.toTypedArray())
                        .build()
                        .remove()

                if (deleteLocalNotReturnedFromServer) {
                    on<StoreHandler>().removeAllExcept(clazz, idProperty, serverIdList, false)
                }

                on<StoreHandler>().store.box(clazz).put(objsToAdd)
            })
        }
    }

    fun handleLifestylesAndGoals(phoneResult: PhoneResult) {
        handleFullListResult(phoneResult.goals, Goal::class.java, Goal_.id, false, { on<ApiModelHandler>().from(it) }, { goal, goalResult -> on<ApiModelHandler>().updateFrom(goal, goalResult) })
        handleFullListResult(phoneResult.lifestyles, Lifestyle::class.java, Lifestyle_.id, false, { on<ApiModelHandler>().from(it) }, { lifestyle, lifestyleResult -> on<ApiModelHandler>().updateFrom(lifestyle, lifestyleResult) })
    }

    private fun handleGroups(groups: List<GroupResult>, deleteLocal: Boolean = false) {
        handleFullListResult(groups, Group::class.java, Group_.id, deleteLocal, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) })
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

        on<StoreHandler>().findAll(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds).observer { existingGroupContacts ->
            val existingGroupContactsMap = HashMap<String, GroupContact>()
            for (existingGroupContact in existingGroupContacts) {
                existingGroupContactsMap[existingGroupContact.id!!] = existingGroupContact
            }

            val groupContactsToAdd = mutableListOf<GroupContact>()

            for (groupContactResult in groupContacts) {
                if (!existingGroupContactsMap.containsKey(groupContactResult.id)) {
                    groupContactsToAdd.add(GroupContactResult.from(groupContactResult))
                } else {
                    existingGroupContactsMap[groupContactResult.id]?.let { existing ->
                        existing.contactName = groupContactResult.phone!!.name
                        existing.contactActive = groupContactResult.phone!!.updated
                        existing.photo = groupContactResult.photo
                        existing.status = groupContactResult.status
                        existing.inviter = groupContactResult.inviter
                        existing.created = groupContactResult.created
                        on<StoreHandler>().store.box(GroupContact::class).put(existing)
                    }
                }
            }

            on<StoreHandler>().store.box(GroupContact::class).put(groupContactsToAdd)

            if (removeAllExcept) {
                on<StoreHandler>().removeAllExcept(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds)
            }
        }
    }

    private fun handlePhones(phoneResults: List<PhoneResult>) {
        handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false, { on<ApiModelHandler>().from(it) }, { phone, phoneResult -> on<ApiModelHandler>().updateFrom(phone, phoneResult) })
    }
}
