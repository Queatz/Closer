package closer.vlllage.com.closer.handler.call

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.CallActivity
import closer.vlllage.com.closer.CallActivity.Companion.EXTRA_ANSWER
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class CallActivityTransitionHandler constructor(private val on: On) {
    fun show(view: View?, phoneId: String, phoneName: String? = null, incoming: Boolean = false) {
        val intent = Intent(on<ApplicationHandler>().app, CallActivity::class.java)
        intent.action = Intent.ACTION_CALL
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.putExtra(CallActivity.EXTRA_CALL_PHONE_ID, phoneId)

        if (incoming) {
            intent.putExtra(CallActivity.EXTRA_INCOMING, true)
        }

        phoneName?.let {
            intent.putExtra(CallActivity.EXTRA_CALL_PHONE_NAME, it)
        }

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        if (incoming) {
            intent.putExtra(EXTRA_ANSWER, true)
            on<NotificationHandler>().showIncomingCallNotification(phoneId, intent)
        } else {
            on<ApplicationHandler>().app.startActivity(intent)
        }
    }
}
