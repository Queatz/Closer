package closer.vlllage.com.closer.handler.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import closer.vlllage.com.closer.Background
import closer.vlllage.com.closer.GroupActivity
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_PHONE_ID
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_RESPOND
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.fromJson
import closer.vlllage.com.closer.extensions.toJson
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.GroupMessageParseHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import java.util.*

class NotificationHandler constructor(private val on: On) {

    fun showBubbleMessageNotification(phone: String, latLng: LatLng?, name: String, message: String) {
        var name = name
        val context = on<ApplicationHandler>().app

        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.reply))
                .build()

        val intent = Intent(context, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        if (latLng != null) {
            intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(latLng.latitude.toFloat(), latLng.longitude.toFloat()))
        }

        name = if (name.isEmpty())
            on<ResourcesHandler>().resources.getString(R.string.app_name)
        else
            name

        intent.putExtra(EXTRA_PHONE_ID, phone)
        intent.putExtra(EXTRA_RESPOND, true)

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationTag = phone + "/" + on<Val>().rndId()

        val backgroundIntent = Intent(context, Background::class.java)
        backgroundIntent.putExtra(EXTRA_PHONE, phone)
        backgroundIntent.putExtra(EXTRA_NOTIFICATION, notificationTag)

        on<StoreHandler>().create(closer.vlllage.com.closer.store.models.Notification::class.java)?.apply {
            created = Date()
            updated = Date()
            this.name = name
            this.message = message
            intentTarget = intent.component!!.className
            intentAction = intent.action
            intentBundle = intent.extras?.toJson(on())
            on<StoreHandler>().store.box(closer.vlllage.com.closer.store.models.Notification::class).put(this)
        }!!

        show(contentIntent, backgroundIntent, remoteInput, name, message, notificationTag, true)
    }

    fun showInvitedToGroupNotification(invitedBy: String, groupName: String, groupId: String) {
        val context = on<ApplicationHandler>().app

        val intent = Intent(context, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        intent.putExtra(EXTRA_GROUP_ID, groupId)

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = on<StoreHandler>().create(closer.vlllage.com.closer.store.models.Notification::class.java)?.apply {
            created = Date()
            updated = Date()
            name = on<ResourcesHandler>().resources.getString(R.string.app_name)
            message = on<ResourcesHandler>().resources.getString(R.string.invited_to_group_notification, invitedBy, groupName)
            intentTarget = intent.component!!.className
            intentAction = intent.action
            intentBundle = intent.extras?.toJson(on())
            on<StoreHandler>().store.box(closer.vlllage.com.closer.store.models.Notification::class).put(this)
        }!!

        show(contentIntent, null, null, notification.name!!,
                notification.message!!,
                "$groupId/invited", true)
    }

    fun showGroupMessageNotification(text: String, messageFrom: String, groupName: String, groupId: String, isPassive: String?) {
        val context = on<ApplicationHandler>().app

        val intent = Intent(context, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        intent.putExtra(EXTRA_GROUP_ID, groupId)

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        on<DisposableHandler>().add(on<GroupMessageParseHandler>().parseString(text).subscribe({ parsedText ->
            val notification = on<StoreHandler>().create(closer.vlllage.com.closer.store.models.Notification::class.java)?.apply {
                created = Date()
                updated = Date()
                name = on<ResourcesHandler>().resources.getString(R.string.group_message_notification, messageFrom, groupName)
                message = parsedText
                intentTarget = intent.component!!.className
                intentAction = intent.action
                intentBundle = intent.extras?.toJson(on())
                on<StoreHandler>().store.box(closer.vlllage.com.closer.store.models.Notification::class).put(this)
            }!!

            show(contentIntent, null, null,
                    notification.name!!,
                    notification.message!!,
                    "$groupId/message", !java.lang.Boolean.valueOf(isPassive))
        }, {}))

    }

    fun showGroupMessageReactionNotification(from: String, groupName: String, reaction: String, groupId: String, isPassive: String?) {
        val context = on<ApplicationHandler>().app

        val intent = Intent(context, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        intent.putExtra(EXTRA_GROUP_ID, groupId)

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = on<StoreHandler>().create(closer.vlllage.com.closer.store.models.Notification::class.java)?.apply {
            created = Date()
            updated = Date()
            name = on<ResourcesHandler>().resources.getString(
                    R.string.group_message_reaction_notification,
                    from,
                    groupName.ifBlank { on<ResourcesHandler>().resources.getString(R.string.unknown) }
            )
            message = reaction
            intentTarget = intent.component!!.className
            intentAction = intent.action
            intentBundle = intent.extras?.toJson(on())
            on<StoreHandler>().store.box(closer.vlllage.com.closer.store.models.Notification::class).put(this)
        }!!

        show(contentIntent, null, null,
                notification.name!!,
                notification.message!!,
                "$groupId/message/reaction", !java.lang.Boolean.valueOf(isPassive))
    }

    fun showEventNotification(event: Event) {
        val context = on<ApplicationHandler>().app

        val intent = Intent(context, GroupActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        intent.putExtra(EXTRA_GROUP_ID, event.groupId)

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = on<StoreHandler>().create(closer.vlllage.com.closer.store.models.Notification::class.java)?.apply {
            created = Date()
            updated = Date()
            name = on<ResourcesHandler>().resources.getString(R.string.event_notification, event.name, on<EventDetailsHandler>().formatRelative(event.startsAt!!))
            message = on<EventDetailsHandler>().formatEventDetails(event)
            intentTarget = intent.component!!.className
            intentAction = intent.action
            intentBundle = intent.extras?.toJson(on())
            on<StoreHandler>().store.box(closer.vlllage.com.closer.store.models.Notification::class).put(this)
        }!!

        show(contentIntent, null, null,
                notification.name!!,
                notification.message!!,
                event.id!! + "/group", false)
    }

    fun hide(notificationTag: String) {
        val notificationManager = NotificationManagerCompat.from(on<ApplicationHandler>().app)
        notificationManager.cancel(notificationTag, NOTIFICATION_ID)
    }

    fun launch(notification: closer.vlllage.com.closer.store.models.Notification) {
        val context = on<ApplicationHandler>().app

        val intent = Intent(notification.intentAction)
        intent.component = ComponentName(
                context,
                notification.intentTarget!!
        )
        intent.putExtras(Bundle().fromJson(on(), notification.intentBundle!!))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        context.startActivity(intent)
    }

    private fun show(contentIntent: PendingIntent, backgroundIntent: Intent?,
                     remoteInput: RemoteInput?,
                     name: String,
                     message: String,
                     notificationTag: String,
                     sound: Boolean) {
        val context = on<ApplicationHandler>().app

        if (on<PersistenceHandler>().isNotificationsPaused) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannel(),
                    context.getString(R.string.closer_notifications),
                    NotificationManager.IMPORTANCE_DEFAULT)
            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, notificationChannel())
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(name)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)

        if (sound) {
            builder.setDefaults(Notification.DEFAULT_ALL)
        } else {
            builder.setDefaults(Notification.DEFAULT_LIGHTS)
        }

        if (remoteInput != null) {
            val replyPendingIntent = PendingIntent.getBroadcast(context,
                    REQUEST_CODE_NOTIFICATION,
                    backgroundIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            val action = NotificationCompat.Action.Builder(R.drawable.ic_notification,
                    on<ResourcesHandler>().resources.getString(R.string.reply), replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .build()

            builder.addAction(action)
        }

        val muteBackgroundIntent = Intent(context, Background::class.java)
        muteBackgroundIntent.putExtra(EXTRA_MUTE, true)
        muteBackgroundIntent.putExtra(EXTRA_NOTIFICATION, notificationTag)

        val mutePendingIntent = PendingIntent.getBroadcast(context,
                REQUEST_CODE_NOTIFICATION_MUTE,
                muteBackgroundIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val muteAction = NotificationCompat.Action.Builder(R.drawable.ic_notifications_paused_white_24dp,
                on<ResourcesHandler>().resources.getString(R.string.mute), mutePendingIntent)
                .build()

        builder.addAction(muteAction)

        val newMessageNotification = builder.build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationTag, NOTIFICATION_ID, newMessageNotification)
    }

    private fun notificationChannel(): String {
        return on<ResourcesHandler>().resources.getString(R.string.notification_channel)
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val NOTIFICATION_ID = 0
        private const val REQUEST_CODE_NOTIFICATION = 101
        private const val REQUEST_CODE_NOTIFICATION_MUTE = 102
        const val EXTRA_NOTIFICATION = "notification"
        const val EXTRA_MUTE = "mute"
    }
}