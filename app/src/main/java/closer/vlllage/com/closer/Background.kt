package closer.vlllage.com.closer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
import closer.vlllage.com.closer.handler.call.CallActivityTransitionHandler.Companion.EXTRA_IGNORE_CALL
import closer.vlllage.com.closer.handler.call.CallConnectionHandler
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler.Companion.EXTRA_MUTE
import closer.vlllage.com.closer.handler.data.NotificationHandler.Companion.EXTRA_NOTIFICATION
import closer.vlllage.com.closer.handler.data.NotificationHandler.Companion.KEY_TEXT_REPLY
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler

class Background : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent ?: return

        val app = context.applicationContext as App

        if (intent.getBooleanExtra(EXTRA_IGNORE_CALL, false)) {
            intent.getStringExtra(EXTRA_PHONE)?.let { app.on<CallConnectionHandler>().endCall(it) }
            app.on<NotificationHandler>().hideFullScreen()
        } else if (intent.getBooleanExtra(EXTRA_MUTE, false)) {
            app.on<PersistenceHandler>().isNotificationsPaused = true
            app.on<ToastHandler>().show(R.string.all_notifications_muted)
        } else {
            val remoteInput = RemoteInput.getResultsFromIntent(intent)
            if (remoteInput != null) {
                val phone = intent.getStringExtra(EXTRA_PHONE)
                val replyMessage = remoteInput.getCharSequence(KEY_TEXT_REPLY)

                if (replyMessage == null || replyMessage.isEmpty()) {
                    return
                }

                if (phone == null) {
                    return
                }

                app.on<ApiHandler>().setAuthorization(app.on<AccountHandler>().phone)

                app.on<DisposableHandler>().add(app.on<ApiHandler>().sendMessage(phone, replyMessage.toString()).subscribe({ successResult ->
                    if (successResult.success) {
                        app.on<ToastHandler>().show(R.string.message_sent)
                    } else {
                        app.on<ToastHandler>().show(R.string.message_not_sent)
                    }
                }, { app.on<ToastHandler>().show(R.string.message_not_sent) }))
            }
        }

        if (intent.hasExtra(EXTRA_NOTIFICATION)) {
            app.on<NotificationHandler>().hide(intent.getStringExtra(EXTRA_NOTIFICATION)!!)
        }
    }
}
