package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.*
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ListEqual
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.SubscriptionBuilder
import java.util.*

class RefreshHandler : PoolMember() {

    fun refreshAll() {
        refreshMyGroups()
        refreshMyMessages()
    }

    fun refreshMyMessages() {
        `$`(LocationHandler::class.java).getCurrentLocation { location ->
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).myMessages(LatLng(
                    location.latitude,
                    location.longitude
            )).subscribe({ this.handleMessages(it) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
        }
    }

    fun refreshMyGroups() {
        `$`(LocationHandler::class.java).getCurrentLocation { location ->
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).myGroups(LatLng(
                    location.latitude,
                    location.longitude
            )).subscribe({ stateResult ->
                handleFullListResult(stateResult.groups, Group::class.java, Group_.id, true, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) })
                handleFullListResult(stateResult.groupInvites, GroupInvite::class.java, GroupInvite_.id, true, { GroupInviteResult.from(it) }, null)
                handleGroupContacts(stateResult.groupContacts!!)
            }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
        }
    }

    fun refreshGroupContacts(groupId: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getContacts(groupId).subscribe({ this.handleGroupContacts(it) },
                { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshEvents(latLng: LatLng) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getEvents(latLng).subscribe({ eventResults -> handleFullListResult(eventResults, Event::class.java, Event_.id, false, { EventResult.from(it) }, { event, eventResult -> EventResult.updateFrom(event, eventResult) }) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshPhysicalGroups(latLng: LatLng) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getPhysicalGroups(latLng).subscribe({ groupResults -> handleFullListResult(groupResults, Group::class.java, Group_.id, false, { GroupResult.from(it) }, { group, groupResult -> GroupResult.updateFrom(group, groupResult) }) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))

        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).myMessages(latLng)
                .subscribe({ this.handleMessages(it) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshGroupActions(groupId: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroupActions(groupId).subscribe({ groupActionResults ->
            val removeQuery = `$`(StoreHandler::class.java).store.box(GroupAction::class.java).query()
                    .equal(GroupAction_.group, groupId)

            for (groupActionResult in groupActionResults) {
                removeQuery.notEqual(GroupAction_.id, groupActionResult.id!!)
            }

            val removeIds = removeQuery.build().findIds()
            `$`(StoreHandler::class.java).store.box(GroupAction::class.java).remove(*removeIds)

            handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) })
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshGroupActions(latLng: LatLng) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroupActions(latLng).subscribe({ groupActionResults -> handleFullListResult(groupActionResults, GroupAction::class.java, GroupAction_.id, false, { GroupActionResult.from(it) }, { groupAction, groupActionResult -> GroupActionResult.updateFrom(groupAction, groupActionResult) }) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshPins(groupId: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getPins(groupId).subscribe({ pinResults ->
            val groupMessageResults = ArrayList<GroupMessageResult>()

            for (pinResult in pinResults) {
                groupMessageResults.add(pinResult.message!!)
            }

            handleMessages(groupMessageResults)

            `$`(StoreHandler::class.java).store.box(Pin::class.java).query().equal(Pin_.to, groupId).build().remove()
            handleFullListResult(pinResults, Pin::class.java, Pin_.id, false, { PinResult.from(it) }, { pin, pinResult -> PinResult.updateFrom(pin, pinResult) })
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshGroupMessages(groupId: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroupMessages(groupId)
                .subscribe({ this.handleMessages(it) }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refreshGroupMessage(groupMessageId: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroupMessage(groupMessageId)
                .subscribe({ groupMessageResult -> refresh(GroupMessageResult.from(groupMessageResult)) },
                        { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    fun refresh(event: Event) {
        refreshObject(event, Event::class.java, Event_.id)
    }

    fun refresh(group: Group) {
        refreshObject(group, Group::class.java, Group_.id)
    }

    fun refresh(phone: Phone) {
        refreshObject(phone, Phone::class.java, Phone_.id)
    }

    fun refresh(groupMessage: GroupMessage) {
        refreshObject(groupMessage, GroupMessage::class.java, GroupMessage_.id)
    }

    fun <T : BaseObject> refreshObject(`object`: T, clazz: Class<T>, idProperty: Property<T>) {
        (`$`(StoreHandler::class.java).store.box(clazz)
                .query()
                .equal(idProperty, `object`.id!!)
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread()) as SubscriptionBuilder<List<T>>)
                .observer { results ->
                    if (!results.isEmpty()) {
                        `object`.objectBoxId = results[0].objectBoxId
                    }
                    `$`(StoreHandler::class.java).store.box(clazz).put(`object`)
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

        val query = `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).query()

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

                    val groupMessageBox = `$`(StoreHandler::class.java).store.box(GroupMessage::class.java)
                    for (message in messages) {
                        if (!existingObjsMap.containsKey(message.id)) {
                            groupMessageBox.put(GroupMessageResult.from(message))
                        } else {
                            val existing = existingObjsMap[message.id]

                            if (existing != null && !`$`(ListEqual::class.java).isEqual(message.reactions, existing.reactions)) {
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

        `$`(StoreHandler::class.java).findAll<T>(clazz, idProperty, serverIdList).observer { existingObjs ->
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

            `$`(StoreHandler::class.java).store.box(clazz).put(objsToAdd)

            if (deleteLocalNotReturnedFromServer) {
                `$`(StoreHandler::class.java).removeAllExcept<T>(clazz, idProperty, serverIdList)
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

        `$`(StoreHandler::class.java).findAll(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds).observer { existingGroupContacts ->
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
                    `$`(StoreHandler::class.java).store.box(GroupContact::class.java).put(
                            existingGroupContactsMap[groupContactResult.id]!!
                    )
                }
            }

            `$`(StoreHandler::class.java).store.box(GroupContact::class.java).put(groupsToAdd)
            `$`(StoreHandler::class.java).removeAllExcept(GroupContact::class.java, GroupContact_.id, allMyGroupContactIds)
        }
    }

    private fun handlePhones(phoneResults: List<PhoneResult>) {
        handleFullListResult(phoneResults, Phone::class.java, Phone_.id, false, { PhoneResult.from(it) }, { phone, phoneResult -> PhoneResult.updateFrom(phone, phoneResult) })
    }
}
