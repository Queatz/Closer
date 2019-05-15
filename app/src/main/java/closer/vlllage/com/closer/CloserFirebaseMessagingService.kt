package closer.vlllage.com.closer

import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.TopHandler
import closer.vlllage.com.closer.store.models.Event
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloserFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String?) {
        val deviceToken = FirebaseInstanceId.getInstance().token
        (application as App).on<AccountHandler>().updateDeviceToken(deviceToken!!)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        val app = application as App
        val data = remoteMessage!!.data
        if (data.isNotEmpty()) {
            if (data.containsKey("action")) {
                when (data["action"]) {
                    "event" -> app.on<NotificationHandler>().showEventNotification(
                            app.on<JsonHandler>().from(data["event"]!!, Event::class.java))
                    "group.invited" -> {
                        app.on<NotificationHandler>().showInvitedToGroupNotification(
                                data["invitedBy"]!!,
                                data["groupName"]!!,
                                data["groupId"]!!)

                        app.on<RefreshHandler>().refreshMyGroups()
                        app.on<RefreshHandler>().refreshGroupMessages(data["groupId"]!!)
                    }
                    "group.message" -> {
                        if (!app.on<TopHandler>().isGroupActive(data["groupId"]!!)) {
                            app.on<NotificationHandler>().showGroupMessageNotification(
                                    data["text"]!!,
                                    data["messageFrom"]!!,
                                    data["groupName"]!!,
                                    data["groupId"]!!,
                                    data["passive"]!!)
                        }

                        app.on<RefreshHandler>().refreshGroupMessages(data["groupId"]!!)
                    }
                    "refresh" -> if (data.containsKey("what")) {
                        when (data["what"]!!) {
                            "groups" -> app.on<RefreshHandler>().refreshMyGroups()
                            "messages" -> app.on<RefreshHandler>().refreshMyMessages()
                        }
                    }
                    "message" -> {
                        val latLng = if (data.containsKey("latLng")) app.on<LatLngStr>().to(data["latLng"]!!) else null
                        app.on<NotificationHandler>().showBubbleMessageNotification(
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
