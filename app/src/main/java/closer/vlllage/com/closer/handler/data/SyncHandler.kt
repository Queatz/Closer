package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.CreateResult
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.HttpEncode
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.reactivex.Single

class SyncHandler : PoolMember() {
    fun syncAll() {
        syncAll(Suggestion::class.java, Suggestion_.localOnly)
        syncAll(Group::class.java, Group_.localOnly)
        syncAll(GroupMessage::class.java, GroupMessage_.localOnly)
        syncAll(Event::class.java, Event_.localOnly)
        syncAll(GroupAction::class.java, GroupAction_.localOnly)
        syncAll(GroupMember::class.java, GroupMember_.localOnly)
    }

    fun <T : BaseObject> sync(obj: T) {
        sync(obj, null)
    }

    fun <T : BaseObject> sync(obj: T, onSyncResult: OnSyncResult?) {
        send(obj, onSyncResult)
    }

    private fun <T : BaseObject> syncAll(clazz: Class<T>, localOnlyProperty: Property<T>) {
        `$`(StoreHandler::class.java).store.box(clazz).query()
                .equal(localOnlyProperty, true)
                .build().subscribe().single().on(AndroidScheduler.mainThread())
                .observer { this.syncAll(it) }
    }

    private fun syncAll(objs: List<BaseObject>) {
        for (obj in objs) {
            sync(obj)
        }
    }

    private fun <T : BaseObject> send(obj: T, onSyncResult: OnSyncResult?) {
        when (obj) {
            is Group -> sendCreateGroup(obj as Group, onSyncResult)
            is Suggestion -> sendCreateSuggestion(obj as Suggestion, onSyncResult)
            is GroupMessage -> sendCreateGroupMessage(obj as GroupMessage, onSyncResult)
            is Event -> sendCreateEvent(obj as Event, onSyncResult)
            is GroupAction -> sendCreateGroupAction(obj as GroupAction, onSyncResult)
            is GroupMember -> sendUpdateGroupMember(obj as GroupMember, onSyncResult)
            else -> throw RuntimeException("Unknown object type for sync: $obj")
        }
    }

    private fun sendCreateGroupAction(groupAction: GroupAction, onSyncResult: OnSyncResult?) {
        groupAction.localOnly = true
        `$`(StoreHandler::class.java).store.box(GroupAction::class.java).put(groupAction)

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).createGroupAction(
                groupAction.group!!,
                groupAction.name!!,
                groupAction.intent!!
        ).subscribe({ createResult ->
            if (createResult.success) {
                groupAction.id = createResult.id
                groupAction.localOnly = false
                `$`(StoreHandler::class.java).store.box(GroupAction::class.java).put(groupAction)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    private fun sendUpdateGroupMember(groupMember: GroupMember, onSyncResult: OnSyncResult?) {
        groupMember.localOnly = true
        `$`(StoreHandler::class.java).store.box(GroupMember::class.java).put(groupMember)

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updateGroupMember(
                groupMember.group!!,
                groupMember.muted,
                groupMember.subscribed
        ).subscribe({ createResult ->
            if (createResult.success) {
                groupMember.id = createResult.id
                groupMember.localOnly = false
                `$`(StoreHandler::class.java).store.box(GroupMember::class.java).put(groupMember)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    private fun sendCreateEvent(event: Event, onSyncResult: OnSyncResult?) {
        event.localOnly = true
        `$`(StoreHandler::class.java).store.box(Event::class.java).put(event)

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).createEvent(
                event.name!!,
                event.about!!,
                event.isPublic,
                LatLng(event.latitude!!, event.longitude!!),
                event.startsAt!!,
                event.endsAt!!
        ).subscribe({ createResult ->
            if (createResult.success) {
                event.id = createResult.id
                event.localOnly = false
                `$`(StoreHandler::class.java).store.box(Event::class.java).put(event)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    private fun sendCreateSuggestion(suggestion: Suggestion, onSyncResult: OnSyncResult?) {
        suggestion.localOnly = true
        `$`(StoreHandler::class.java).store.box(Suggestion::class.java).put(suggestion)

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).addSuggestion(
                suggestion.name!!,
                LatLng(suggestion.latitude!!, suggestion.longitude!!)
        ).subscribe({ createResult ->
            if (createResult.success) {
                suggestion.id = createResult.id
                suggestion.localOnly = false
                `$`(StoreHandler::class.java).store.box(Suggestion::class.java).put(suggestion)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    private fun sendCreateGroup(group: Group, onSyncResult: OnSyncResult?) {
        group.localOnly = true
        `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)

        val createApiRequest: Single<CreateResult>

        if (group.physical) {
            createApiRequest = `$`(ApiHandler::class.java).createPhysicalGroup(LatLng(
                    group.latitude!!,
                    group.longitude!!
            ))
        } else if (group.isPublic) {
            createApiRequest = `$`(ApiHandler::class.java).createPublicGroup(group.name!!, group.about!!, LatLng(
                    group.latitude!!,
                    group.longitude!!
            ))
        } else {
            createApiRequest = `$`(ApiHandler::class.java).createGroup(group.name!!)
        }

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(createApiRequest.subscribe({ createResult ->
            if (createResult.success) {
                group.id = createResult.id
                group.localOnly = false
                `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

    private fun sendCreateGroupMessage(groupMessage: GroupMessage, onSyncResult: OnSyncResult?) {
        groupMessage.localOnly = true
        `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).put(groupMessage)

        val apiCall = `$`(ApiHandler::class.java).sendGroupMessage(
                groupMessage.to!!,
                groupMessage.text!!,
                `$`(HttpEncode::class.java).encode(groupMessage.attachment)!!
        )

        `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(apiCall.subscribe({ createResult ->
            if (createResult.success) {
                groupMessage.id = createResult.id
                groupMessage.localOnly = false
                `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).put(groupMessage)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> `$`(ConnectionErrorHandler::class.java).notifyConnectionError() }))
    }

}

typealias OnSyncResult = (id: String) -> Unit

