package closer.vlllage.com.closer.handler.call

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.Background
import closer.vlllage.com.closer.CallActivity
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class CallActivityTransitionHandler(private val on: On) {
    fun show(view: View?, phoneId: String, phoneName: String? = null, incoming: Boolean = false) {
        val intent = makeIntent(view, phoneId, phoneName, incoming)

        if (incoming) {
            val answerIntent = Intent(intent)
            answerIntent.putExtra(CallActivity.EXTRA_ANSWER, true)

            val ignoreIntent = Intent(on<ApplicationHandler>().app, Background::class.java)
            ignoreIntent.putExtra(EXTRA_IGNORE_CALL, true)
            ignoreIntent.putExtra(EXTRA_PHONE, phoneId)

            on<NotificationHandler>().showIncomingCallNotification(phoneId, intent, answerIntent, ignoreIntent)
        } else {
            on<ApplicationHandler>().app.startActivity(intent)
        }
    }

    private fun makeIntent(view: View?, phoneId: String, phoneName: String?, incoming: Boolean): Intent {
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

        return intent
    }

    companion object {
        const val EXTRA_IGNORE_CALL = "ignoreCall"
    }
}
