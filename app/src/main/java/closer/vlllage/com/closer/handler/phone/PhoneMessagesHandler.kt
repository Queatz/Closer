package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.ui.CircularRevealActivity

class PhoneMessagesHandler : PoolMember() {
    fun openMessagesWithPhone(phoneId: String, name: String, status: String) {
        val runnable = { `$`(MapActivityHandler::class.java).replyToPhone(phoneId, name, status, null) }

        if (`$`(ActivityHandler::class.java).activity is CircularRevealActivity) {
            (`$`(ActivityHandler::class.java).activity as CircularRevealActivity).finish(runnable)
        } else {
            runnable.invoke()
        }
    }
}
