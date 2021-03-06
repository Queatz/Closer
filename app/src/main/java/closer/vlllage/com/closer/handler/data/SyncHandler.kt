package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.models.CreateResult
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.Property
import io.objectbox.android.AndroidScheduler
import io.reactivex.Single

class SyncHandler constructor(private val on: On) {
    fun syncAll() {
        syncAll(Suggestion::class.java, Suggestion_.localOnly)
        syncAll(Group::class.java, Group_.localOnly)
        syncAll(GroupMessage::class.java, GroupMessage_.localOnly)
        syncAll(Event::class.java, Event_.localOnly)
        syncAll(GroupAction::class.java, GroupAction_.localOnly)
        syncAll(GroupMember::class.java, GroupMember_.localOnly)
        syncAll(Quest::class.java, Quest_.localOnly)
        syncAll(QuestProgress::class.java, QuestProgress_.localOnly)
        syncAll(Story::class.java, Story_.localOnly)
    }

    fun <T : BaseObject> sync(obj: T, onSyncResult: OnSyncResult? = null) {
        send(obj, onSyncResult)
    }

    private fun <T : BaseObject> syncAll(clazz: Class<T>, localOnlyProperty: Property<T>) {
        on<StoreHandler>().store.box(clazz).query()
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
            is Group -> sendCreateGroup(obj, onSyncResult)
            is Suggestion -> sendCreateSuggestion(obj, onSyncResult)
            is Story -> sendCreateStory(obj, onSyncResult)
            is GroupMessage -> sendCreateGroupMessage(obj, onSyncResult)
            is Event -> sendCreateEvent(obj, onSyncResult)
            is GroupAction -> sendCreateGroupAction(obj, onSyncResult)
            is GroupMember -> sendUpdateGroupMember(obj, onSyncResult)
            is Quest -> { sendCreateQuest(obj, onSyncResult) }
            is QuestProgress -> { sendCreateQuestProgress(obj, onSyncResult) }
            else -> throw RuntimeException("Unknown object type for sync: $obj")
        }
    }

    private fun sendCreateQuest(quest: Quest, onSyncResult: OnSyncResult?) {
        quest.localOnly = true
        on<StoreHandler>().store.box(Quest::class).put(quest)

        on<ApiHandler>().createQuest(
                quest.name!!,
                quest.isPublic,
                on<LatLngStr>().to(quest.latitude!!, quest.longitude!!),
                quest.flow!!
        ).subscribe({ createResult ->
            if (createResult.success) {
                quest.id = createResult.id
                quest.localOnly = false
                on<StoreHandler>().store.box(Quest::class).put(quest)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, {
            on<ConnectionErrorHandler>().notifyConnectionError()
        }).also {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(it)
        }
    }

    private fun sendCreateQuestProgress(questProgress: QuestProgress, onSyncResult: OnSyncResult?) {
        questProgress.localOnly = true
        on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)

        on<ApiHandler>().createQuestProgress(
                questProgress.questId!!,
                questProgress.ofId!!,
                questProgress.isPublic,
                questProgress.active ?: true,
                questProgress.progress!!
        ).subscribe({ createResult ->
            if (createResult.success) {
                questProgress.id = createResult.id
                questProgress.localOnly = false
                on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, {
            on<ConnectionErrorHandler>().notifyConnectionError()
        }).also {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(it)
        }
    }

    private fun sendCreateGroupAction(groupAction: GroupAction, onSyncResult: OnSyncResult?) {
        groupAction.localOnly = true
        on<StoreHandler>().store.box(GroupAction::class).put(groupAction)

        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().createGroupAction(
                groupAction.group!!,
                groupAction.name!!,
                groupAction.intent!!
        ).subscribe({ createResult ->
            if (createResult.success) {
                groupAction.id = createResult.id
                groupAction.localOnly = false
                on<StoreHandler>().store.box(GroupAction::class).put(groupAction)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendUpdateGroupMember(groupMember: GroupMember, onSyncResult: OnSyncResult?) {
        groupMember.localOnly = true
        on<StoreHandler>().store.box(GroupMember::class).put(groupMember)

        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().updateGroupMember(
                groupMember.group!!,
                groupMember.muted,
                groupMember.subscribed
        ).subscribe({ createResult ->
            if (createResult.success) {
                groupMember.id = createResult.id
                groupMember.localOnly = false
                on<StoreHandler>().store.box(GroupMember::class).put(groupMember)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendCreateEvent(event: Event, onSyncResult: OnSyncResult?) {
        event.localOnly = true
        on<StoreHandler>().store.box(Event::class).put(event)

        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().createEvent(
                event.name!!,
                event.about!!,
                event.isPublic,
                LatLng(event.latitude!!, event.longitude!!),
                event.startsAt!!,
                event.endsAt!!,
                event.allDay
        ).subscribe({ createResult ->
            if (createResult.success) {
                event.id = createResult.id
                event.localOnly = false
                on<StoreHandler>().store.box(Event::class).put(event)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendCreateSuggestion(suggestion: Suggestion, onSyncResult: OnSyncResult?) {
        suggestion.localOnly = true
        on<StoreHandler>().store.box(Suggestion::class).put(suggestion)

        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().addSuggestion(
                suggestion.name!!,
                LatLng(suggestion.latitude!!, suggestion.longitude!!)
        ).subscribe({ createResult ->
            if (createResult.success) {
                suggestion.id = createResult.id
                suggestion.localOnly = false
                on<StoreHandler>().store.box(Suggestion::class).put(suggestion)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendCreateStory(story: Story, onSyncResult: OnSyncResult?) {
        story.localOnly = true
        on<StoreHandler>().store.box(Story::class).put(story)

        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().addStory(
                story.text,
                story.photo,
                LatLng(story.latitude!!, story.longitude!!)
        ).subscribe({ createResult ->
            if (createResult.success) {
                story.id = createResult.id
                story.localOnly = false
                on<StoreHandler>().store.box(Story::class).put(story)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendCreateGroup(group: Group, onSyncResult: OnSyncResult?) {
        group.localOnly = true
        on<StoreHandler>().store.box(Group::class).put(group)

        val createApiRequest: Single<CreateResult> = if (group.physical) {
            on<ApiHandler>().createPhysicalGroup(group.name ?: "", group.about ?: "", group.isPublic, LatLng(
                    group.latitude!!,
                    group.longitude!!
            ), group.hub)
        } else if (group.isPublic) {
            on<ApiHandler>().createPublicGroup(group.name!!, group.about!!, LatLng(
                    group.latitude!!,
                    group.longitude!!
            ))
        } else {
            on<ApiHandler>().createGroup(group.name!!)
        }

        on<ApplicationHandler>().app.on<DisposableHandler>().add(createApiRequest.subscribe({ createResult ->
            if (createResult.success) {
                group.id = createResult.id
                group.localOnly = false
                on<StoreHandler>().store.box(Group::class).put(group)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

    private fun sendCreateGroupMessage(groupMessage: GroupMessage, onSyncResult: OnSyncResult?) {
        groupMessage.localOnly = true
        on<StoreHandler>().store.box(GroupMessage::class).put(groupMessage)

        val apiCall = on<ApiHandler>().sendGroupMessage(
                groupMessage.to!!,
                groupMessage.text,
                on<HttpEncode>().encode(groupMessage.attachment)
        )

        on<ApplicationHandler>().app.on<DisposableHandler>().add(apiCall.subscribe({ createResult ->
            if (createResult.success) {
                groupMessage.id = createResult.id
                groupMessage.localOnly = false
                on<StoreHandler>().store.box(GroupMessage::class).put(groupMessage)
                onSyncResult?.invoke(createResult.id!!)
            }
        }, { error -> on<ConnectionErrorHandler>().notifyConnectionError() }))
    }

}

typealias OnSyncResult = (id: String) -> Unit

