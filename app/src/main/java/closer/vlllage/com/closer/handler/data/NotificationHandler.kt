package closer.vlllage.com.closer.handler.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import closer.vlllage.com.closer.Background
import closer.vlllage.com.closer.CallActivity.Companion.EXTRA_CALL_PHONE_ID
import closer.vlllage.com.closer.CallActivity.Companion.EXTRA_CALL_PHONE_NAME
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
import closer.vlllage.com.closer.handler.phone.NameHandler
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

    fun showGroupMessageNotification(text: String, messageFrom: String, groupName: String?, direct: String?, groupId: String, isPassive: String?) {
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
                name = groupName?.let { on<ResourcesHandler>().resources.getString(R.string.group_message_notification, messageFrom, it) } ?: messageFrom
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

    fun showGroupMessageReactionNotification(from: String, groupName: String?, reaction: String, groupId: String, isPassive: String?) {
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
            name = groupName?.let {
                on<ResourcesHandler>().resources.getString(
                        R.string.group_message_reaction_notification_in,
                        from,
                        it.ifBlank { on<ResourcesHandler>().resources.getString(R.string.unknown) }
                )
            } ?: on<ResourcesHandler>().resources.getString(
                    R.string.group_message_reaction_notification,
                    from)
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

    fun showIncomingCallNotification(phoneId: String, intent: Intent, answerIntent: Intent, ignoreIntent: Intent) {
        val context = on<ApplicationHandler>().app

        val contentIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_NOTIFICATION,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        )

        val phoneName = on<NameHandler>().getFallbackName(
                intent.getStringExtra(EXTRA_CALL_PHONE_ID),
                intent.getStringExtra(EXTRA_CALL_PHONE_NAME)
        )

        show(contentIntent, null, null,
                "Call from $phoneName",
                "",
                "$phoneId/call", false, intent, answerIntent, ignoreIntent)
    }

    fun hide(notificationTag: String) {
        val notificationManager = NotificationManagerCompat.from(on<ApplicationHandler>().app)
        notificationManager.cancel(notificationTag, NOTIFICATION_ID)
    }

    fun hideFullScreen() {
        val notificationManager = NotificationManagerCompat.from(on<ApplicationHandler>().app)
        notificationManager.cancel(null, FULLSCREEN_NOTIFICATION_ID)
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

    private fun show(contentIntent: PendingIntent,
                     backgroundIntent: Intent?,
                     remoteInput: RemoteInput?,
                     name: String,
                     message: String,
                     notificationTag: String,
                     sound: Boolean,
                     fullScreenIntent: Intent? = null,
                     fullScreenAnswerIntent: Intent? = null,
                     fullScreenIgnoreIntent: Intent? = null) {
        val context = on<ApplicationHandler>().app

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel(notificationChannel(),
                    context.getString(R.string.closer_notifications),
                    NotificationManager.IMPORTANCE_DEFAULT).also {
                (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                        .createNotificationChannel(it)
            }

            NotificationChannel(callNotificationChannel(),
                    context.getString(R.string.closer_calls),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build())
                vibrationPattern = longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500)
            }.also {
                (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                        .createNotificationChannel(it)
            }
        }

        val builder = NotificationCompat.Builder(context, when (fullScreenIntent) {
            null -> notificationChannel()
            else -> callNotificationChannel()
        })
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(name)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)

        if (sound) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioManager.STREAM_NOTIFICATION)
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

        fullScreenIntent?.let { it ->
            val pendingIntent = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE_NOTIFICATION,
                    it,
                    PendingIntent.FLAG_ONE_SHOT
            )

            val pendingIgnoreIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_NOTIFICATION_IGNORE,
                    fullScreenIgnoreIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val pendingAnswerIntent = PendingIntent.getActivity(
                    context,
                    REQUEST_CODE_NOTIFICATION_ANSWER,
                    fullScreenAnswerIntent,
                    PendingIntent.FLAG_ONE_SHOT
            )

            builder.addAction(R.drawable.ic_baseline_call_end_24,
                    coloredText(R.string.ignore_call, R.color.red), pendingIgnoreIntent)

            builder.addAction(R.drawable.common_full_open_on_phone,
                    coloredText(R.string.answer_call, R.color.green), pendingAnswerIntent)

            builder.setAutoCancel(true)
            builder.setCategory(NotificationCompat.CATEGORY_CALL)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioManager.STREAM_RING)
            builder.setVibrate(longArrayOf(500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500))

            builder.setOngoing(true)
            builder.setFullScreenIntent(pendingIntent, true)
        }

        val notificationManager = NotificationManagerCompat.from(context)

        if (fullScreenIntent != null) {
            notificationManager.cancel(null, FULLSCREEN_NOTIFICATION_ID)
        }

        notificationManager.notify(when (fullScreenIntent) {
            null -> notificationTag
            else -> null
        }, when (fullScreenIntent) {
            null -> NOTIFICATION_ID
            else -> FULLSCREEN_NOTIFICATION_ID
        }, builder.build())
    }

    private fun coloredText(@StringRes stringRes: Int, @ColorRes colorRes: Int): Spannable? {
        val spannable: Spannable = SpannableString(on<ResourcesHandler>().resources.getText(stringRes))
        if (VERSION.SDK_INT >= VERSION_CODES.N_MR1) {
            spannable.setSpan(
                    ForegroundColorSpan(on<ResourcesHandler>().resources.getColor(colorRes)), 0, spannable.length, 0)
        }
        return spannable
    }

    private fun notificationChannel(): String {
        return on<ResourcesHandler>().resources.getString(R.string.notification_channel)
    }

    private fun callNotificationChannel(): String {
        return on<ResourcesHandler>().resources.getString(R.string.call_notification_channel)
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val NOTIFICATION_ID = 0
        const val FULLSCREEN_NOTIFICATION_ID = 1
        private const val REQUEST_CODE_NOTIFICATION = 101
        private const val REQUEST_CODE_NOTIFICATION_ANSWER = 103
        private const val REQUEST_CODE_NOTIFICATION_IGNORE = 104
        const val EXTRA_NOTIFICATION = "notification"
    }
}