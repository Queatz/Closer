package closer.vlllage.com.closer

import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.TopHandler
import closer.vlllage.com.closer.store.models.Event
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloserFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        val app = application as App
        val data = remoteMessage!!.data
        if (data.isNotEmpty()) {
            if (data.containsKey("action")) {
                when (data["action"]) {
                    "event" -> app.`$`(NotificationHandler::class.java).showEventNotification(
                            app.`$`(JsonHandler::class.java).from(data["event"]!!, Event::class.java))
                    "group.invited" -> {
                        app.`$`(NotificationHandler::class.java).showInvitedToGroupNotification(
                                data["invitedBy"]!!,
                                data["groupName"]!!,
                                data["groupId"]!!)

                        app.`$`(RefreshHandler::class.java).refreshMyGroups()
                        app.`$`(RefreshHandler::class.java).refreshGroupMessages(data["groupId"]!!)
                    }
                    "group.message" -> {
                        if (!app.`$`(TopHandler::class.java).isGroupActive(data["groupId"]!!)) {
                            app.`$`(NotificationHandler::class.java).showGroupMessageNotification(
                                    data["text"]!!,
                                    data["messageFrom"]!!,
                                    data["groupName"]!!,
                                    data["groupId"]!!,
                                    data["passive"]!!)
                        }

                        app.`$`(RefreshHandler::class.java).refreshGroupMessages(data["groupId"]!!)
                    }
                    "refresh" -> if (data.containsKey("what")) {
                        when (data["what"]!!) {
                            "groups" -> app.`$`(RefreshHandler::class.java).refreshMyGroups()
                            "messages" -> app.`$`(RefreshHandler::class.java).refreshMyMessages()
                        }
                    }
                    "message" -> {
                        val latLng = if (data.containsKey("latLng")) app.`$`(LatLngStr::class.java).to(data["latLng"]!!) else null
                        app.`$`(NotificationHandler::class.java).showBubbleMessageNotification(
                                data["phone"]!!,
                                latLng,
                                if (data.containsKey("name")) data["name"]!! else "",
                                data["message"]!!)
                    }
                }
            }
        }
    }
}
