package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.*
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ListEqual
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import closer.vlllage.com.closer.store.models.GroupMember_.phone
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.SubscriptionBuilder
import java.util.*

class RefreshHandler constructor(private val on: On) {

    fun refreshAll() {
        refreshMyGroups()
        refreshMyMessages()
    }

    fun refreshMyMessages() {
        on<LocationHandler>().getCurrentLocation { location ->
            on<DisposableHandler>().add(on<ApiHandler>().myMessages(LatLng(
                    location.latitude,
                    location.longitude
            )).subscribe({ this.handleMessages(it) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
        }
    }

    fun refreshMyGroups() {
        on<LocationHandler>().getCurrentLocation { location ->
            on<DisposableHandler>().add(on<ApiHandler>().myGroups(LatLng(
                    location.latitude,
                    location.longitude
            )).subscribe({ stateResult ->
                handleFullListResult(stateResult.groups, Group::class.java, Group_.id, true, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) })
                handleFullListResult(stateResult.groupInvites, GroupInvite::class.java, GroupInvite_.id, true, { GroupInviteResult.from(it) }, null)
                handleGroupContacts(stateResult.groupContacts!!)
            }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
        }
    }

    fun refreshGroupContacts(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getContacts(groupId).subscribe({ this.handleGroupContacts(it) },
                { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshEvents(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getEvents(latLng).subscribe({ eventResults -> handleFullListResult(eventResults, Event::class.java, Event_.id, false, { EventResult.from(it) }, { event, eventResult -> EventResult.updateFrom(event, eventResult) }) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshPhysicalGroups(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getPhysicalGroups(latLng).subscribe({ groupResults -> handleFullListResult(groupResults, Group::class.java, Group_.id, false, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) }) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))

        on<DisposableHandler>().add(on<ApiHandler>().myMessages(latLng)
                .subscribe({ this.handleMessages(it) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
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
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshGroupActions(latLng: LatLng) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupActions(latLng).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) }) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshPins(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getPins(groupId).subscribe({ pinResults ->
            val groupMessageResults = ArrayList<GroupMessageResult>()

            for (pinResult in pinResults) {
                groupMessageResults.add(pinResult.message!!)
            }

            handleMessages(groupMessageResults)

            on<StoreHandler>().store.box(Pin::class).query().equal(Pin_.to, groupId).build().remove()
            handleFullListResult(pinResults, Pin::class.java, Pin_.id, false, { PinResult.from(it) }, { pin, pinResult -> PinResult.updateFrom(pin, pinResult) })
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshGroupMessages(groupId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupMessages(groupId)
                .subscribe({ this.handleMessages(it) }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refreshGroupMessage(groupMessageId: String) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupMessage(groupMessageId)
                .subscribe({ groupMessageResult -> refresh(GroupMessageResult.from(groupMessageResult)) },
                        { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    fun refresh(baseObject: BaseObject) {
        when (baseObject) {
            is Event -> refreshObject(baseObject, Event::class.java, Event_.id)
            is Group -> refreshObject(baseObject, Group::class.java, Group_.id)
            is Phone -> refreshObject(baseObject, Phone::class.java, Phone_.id)
            is GroupMessage -> refreshObject(baseObject, GroupMessage::class.java, GroupMessage_.id)
        }
    }

    fun <T : BaseObject> refreshObject(`object`: T, clazz: Class<T>, idProperty: Property<T>) {
        (on<StoreHandler>().store.box(clazz)
                .query()
                .equal(idProperty, `object`.id!!)
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread()) as SubscriptionBuilder<List<T>>)
                .observer { results ->
                    if (results.isNotEmpty()) {
                        `object`.objectBoxId = results[0].objectBoxId
                    }
                    on<StoreHandler>().store.box(clazz).put(`object`)
                }
    }

    private fun handleMessages(messages: List<GroupMessageResult>) {
        val phoneResults = ArrayList<PhoneResult>()
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
                        if (!existingObjsMap.containsKey(message.id)) {
                            groupMessageBox.put(GroupMessageResult.from(message))
                        } else {
                            val existing = existingObjsMap[message.id]

                            if (existing != null && !on<ListEqual>().isEqual(message.reactions, existing.reactions)) {
                                existing.reactions = message.reactions!!
                                groupMessageBox.put(existing)
                            }
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
        val serverIdList = HashSet<String>()
        for (obj in results!!) {
            serverIdList.add(obj.id!!)
        }

        on<StoreHandler>().findAll(clazz, idProperty, serverIdList).observer { existingObjs ->
            val existingObjsMap = HashMap<String, T>()
            for (existingObj in existingObjs) {
                existingObjsMap[existingObj.id!!] = existingObj
            }

            val objsToAdd = ArrayList<T>()

            for (result in results) {
                if (!existingObjsMap.containsKey(result.id)) {
                    objsToAdd.add(createTransformer.invoke(result))
                } else if (updateTransformer != null) {
                    objsToAdd.add(updateTransformer.invoke(existingObjsMap[result.id]!!, result))
                }
            }

            on<StoreHandler>().store.box(clazz).put(objsToAdd)

            if (deleteLocalNotReturnedFromServer) {
                on<StoreHandler>().removeAllExcept<T>(clazz, idProperty, serverIdList)
            }
        }
    }

    private fun handleGroupContacts(groupContacts: List<GroupContactResult>) {
        val allMyGroupContactIds = HashSet<String>()
        val phoneResults = ArrayList<PhoneResult>()
        for (groupContactResult in groupContacts) {
            allMyGroupContactIds.add(groupContactResult.id!!)

            if (groupContactResult.phone != null) {
                phoneResults.add(groupContactResult.phone!!)
            }
        }

        handlePhones(phoneResults)

        on<StoreHandler>().findAll(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds).observer { existingGroupContacts ->
            val existingGroupContactsMap = HashMap<String, GroupContact>()
            for (existingGroupContact in existingGroupContacts) {
                existingGroupContactsMap[existingGroupContact.id!!] = existingGroupContact
            }

            val groupsToAdd = ArrayList<GroupContact>()

            for (groupContactResult in groupContacts) {
                if (!existingGroupContactsMap.containsKey(groupContactResult.id)) {
                    groupsToAdd.add(GroupContactResult.from(groupContactResult))
                } else {
                    existingGroupContactsMap[groupContactResult.id]!!.contactName = groupContactResult.phone!!.name
                    existingGroupContactsMap[groupContactResult.id]!!.contactActive = groupContactResult.phone!!.updated
                    on<StoreHandler>().store.box(GroupContact::class).put(
                            existingGroupContactsMap[groupContactResult.id]!!
                    )
                }
            }

            on<StoreHandler>().store.box(GroupContact::class).put(groupsToAdd)
            on<StoreHandler>().removeAllExcept(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds)
        }
    }

    private fun handlePhones(phoneResults: List<PhoneResult>) {
        handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false, { PhoneResult.from(it) }, { phone, phoneResult -> PhoneResult.updateFrom(phone, phoneResult) })
    }
}
