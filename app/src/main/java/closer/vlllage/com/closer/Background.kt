package closer.vlllage.com.closer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
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
        val app = context.applicationContext as App

        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_MUTE, false)) {
                app.`$`(PersistenceHandler::class.java).isNotificationsPaused = true
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

                    app.`$`(ApiHandler::class.java).setAuthorization(app.`$`(AccountHandler::class.java).phone)

                    app.`$`(DisposableHandler::class.java).add(app.`$`(ApiHandler::class.java).sendMessage(phone, replyMessage.toString()).subscribe({ successResult ->
                        if (successResult.success) {
                            app.`$`(ToastHandler::class.java).show(R.string.message_sent)
                        } else {
                            app.`$`(ToastHandler::class.java).show(R.string.message_not_sent)
                        }
                    }, { _ -> app.`$`(ToastHandler::class.java).show(R.string.message_not_sent) }))
                }
            }

            if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                app.`$`(NotificationHandler::class.java).hide(intent.getStringExtra(EXTRA_NOTIFICATION))
            }
        }
    }
}
