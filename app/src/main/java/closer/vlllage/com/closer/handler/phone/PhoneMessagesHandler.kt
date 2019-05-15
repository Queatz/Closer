package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On

class PhoneMessagesHandler constructor(private val on: On) {
    fun openMessagesWithPhone(phoneId: String, name: String?, status: String) {
        val runnable = { on<MapActivityHandler>().replyToPhone(phoneId, name, status, null) }

        if (on<ActivityHandler>().activity is CircularRevealActivity) {
            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }
}
