package closer.vlllage.com.closer.handler.data

import at.bluesource.choicesdk.messaging.common.RemoteMessage
import at.bluesource.choicesdk.messaging.factory.MessagingRepositoryFactory
import closer.vlllage.com.closer.handler.call.CallEvent
import closer.vlllage.com.closer.handler.call.CallEventHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.TopHandler
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.observers.DisposableObserver

class PushHandler constructor(private val on: On) {
    fun init() {
        MessagingRepositoryFactory.getMessagingService()
            .getNewTokenObservable()
            .subscribeWith(object : DisposableObserver<String>() {
                override fun onNext(token: String) {
                    on<ApplicationHandler>().app.on<AccountHandler>().updateDeviceToken(token)
                }
                override fun onError(e: Throwable?) {}
                override fun onComplete() {}
            })

        MessagingRepositoryFactory.getMessagingService().requestToken(on<ApplicationHandler>().app)

        MessagingRepositoryFactory.getMessagingService()
            .getMessageReceivedObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableObserver<RemoteMessage>() {
                override fun onNext(remoteMessage: RemoteMessage) {
                    onMessageReceived(remoteMessage.data)
                }

                override fun onError(e: Throwable?) {}
                override fun onComplete() {}
            })
    }

    fun onMessageReceived(data: Map<String, String>) {
        val app = on<ApplicationHandler>().app
        if (data.isNotEmpty()) {
            if (data.containsKey("action")) {
                when (data["action"]) {
                    "call" -> app.on<CallEventHandler>().handle(
                        CallEvent(
                        data["phone"]!!,
                        data["phoneName"],
                        data["event"]!!,
                        app.on<JsonHandler>().from(data["data"]!!, String::class.java)
                    )
                    )
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
                                data["groupName"],
                                data["direct"],
                                data["groupId"]!!,
                                data["passive"])
                        }

                        app.on<RefreshHandler>().refreshGroupMessages(data["groupId"]!!)
                    }
                    "group.message.reaction" -> {
                        if (!app.on<TopHandler>().isGroupActive(data["groupId"]!!)) {
                            app.on<NotificationHandler>().showGroupMessageReactionNotification(
                                data["reactionFrom"]!!,
                                data["groupName"],
                                data["reaction"]!!,
                                data["groupId"]!!,
                                data["passive"])
                        }

                        app.on<RefreshHandler>().refreshGroupMessages(data["groupId"]!!)
                    }
                    "refresh" -> if (data.containsKey("what")) {
                        when (data["what"]!!) {
                            "groups" -> app.on<RefreshHandler>().refreshMyGroups()
                            "messages" -> app.on<RefreshHandler>().refreshMyMessages(data["groupId"])
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