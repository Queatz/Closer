package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.util.*

class GroupMessageAttachmentHandler constructor(private val on: On) {

    fun shareLocation(latLng: LatLng, group: Group): Boolean {
        val suggestion = Suggestion()
        suggestion.latitude = latLng.latitude
        suggestion.longitude = latLng.longitude
        return shareSuggestion(suggestion, group)
    }

    fun shareSuggestion(suggestion: Suggestion, group: Group): Boolean {
        val jsonObject = JsonObject()
        jsonObject.add("suggestion", on<JsonHandler>().toJsonTree(suggestion))

        saveMessageWithAttachment(group.id, null, jsonObject)

        return true
    }

    fun shareEvent(event: Event, group: Group): Boolean {
        val jsonObject = JsonObject()
        jsonObject.add("event", on<JsonHandler>().toJsonTree(event))

        saveMessageWithAttachment(group.id, null, jsonObject)

        return true
    }

    fun shareGroup(groupToShare: Group, group: Group): Boolean {
        val jsonObject = JsonObject()
        jsonObject.add("group", on<JsonHandler>().toJsonTree(groupToShare))

        saveMessageWithAttachment(group.id, null, jsonObject)

        return true
    }

    fun sharePhoto(photoUrl: String, groupId: String): Boolean {
        val jsonObject = JsonObject()
        jsonObject.add("photo", JsonPrimitive(photoUrl))
        saveMessageWithAttachment(groupId, null, jsonObject)
        return true
    }

    fun sharePhoto(photoUrl: String, latLng: LatLng): Boolean {
        val jsonObject = JsonObject()
        jsonObject.add("photo", JsonPrimitive(photoUrl))
        saveMessageWithAttachment(null, latLng, jsonObject)
        return true
    }

    fun groupActionReply(groupId: String, groupAction: GroupAction, comment: String): Boolean {
        if (groupAction.intent == null) {
            on<DefaultAlerts>().thatDidntWork()
            return false
        }

        val jsonObject = JsonObject()
        val action = JsonObject()
        action.add("intent", JsonPrimitive(groupAction.intent!!))
        action.add("comment", JsonPrimitive(comment))
        jsonObject.add("action", action)
        saveMessageWithAttachment(groupId, null, jsonObject)
        return true
    }

    fun shareGroupMessage(groupId: String, groupMessageId: String?): Boolean {
        if (groupMessageId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return false
        }

        val jsonObject = JsonObject()
        jsonObject.add("share", JsonPrimitive(groupMessageId))
        saveMessageWithAttachment(groupId, null, jsonObject)
        return true
    }

    private fun saveMessageWithAttachment(groupId: String?, latLng: LatLng?, jsonObject: JsonObject) {
        val groupMessage = GroupMessage()
        groupMessage.attachment = on<JsonHandler>().to(jsonObject)
        groupMessage.to = groupId

        if (latLng != null) {
            groupMessage.latitude = latLng.latitude
            groupMessage.longitude = latLng.longitude
        }

        groupMessage.from = on<PersistenceHandler>().phoneId
        groupMessage.time = Date()
        on<StoreHandler>().store.box(GroupMessage::class.java).put(groupMessage)
        on<SyncHandler>().sync(groupMessage)
    }

    private fun getGroupContactForGroup(group: Group): GroupContact? {
        return if (on<PersistenceHandler>().phoneId == null) {
            null
        } else on<StoreHandler>().store.box(GroupContact::class.java).query()
                .equal(GroupContact_.contactId, on<PersistenceHandler>().phoneId!!)
                .equal(GroupContact_.groupId, group.id!!)
                .build().findFirst()

    }
}
