package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DefaultInput
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On

class ReplyHandler constructor(private val on: On) {
    fun reply(phoneId: String) {
        on<DefaultInput>().show(R.string.send_message, R.string.say_something, R.string.send) { message ->
            on<DisposableHandler>().add(on<ApiHandler>().sendMessage(phoneId, message).subscribe({ successResult ->
                if (!successResult.success) {
                    on<DefaultAlerts>().thatDidntWork()
                }
            }, { on<DefaultAlerts>().thatDidntWork() }))
        }
    }
}