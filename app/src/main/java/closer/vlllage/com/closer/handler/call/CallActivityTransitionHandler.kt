package closer.vlllage.com.closer.handler.call

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.CallActivity
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class CallActivityTransitionHandler constructor(private val on: On) {
    fun show(view: View?, phoneId: String, incoming: Boolean = false) {
        val intent = Intent(on<ApplicationHandler>().app, CallActivity::class.java)
        intent.action = Intent.ACTION_CALL
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.putExtra(CallActivity.EXTRA_CALL_PHONE_ID, phoneId)

        if (incoming) {
            intent.putExtra(CallActivity.EXTRA_INCOMING, true)
        }

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        on<ApplicationHandler>().app.startActivity(intent)
    }
}
