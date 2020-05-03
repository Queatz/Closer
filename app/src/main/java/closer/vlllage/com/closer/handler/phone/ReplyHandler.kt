package closer.vlllage.com.closer.handler.phone

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DefaultInput
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

class ReplyHandler constructor(private val on: On) {
    fun reply(phone: Phone) {
        reply(phone.id!!, phone.name)
    }

    private fun reply(phoneId: String, phoneName: String? = null) {
        on<DefaultInput>().show(
                on<ResourcesHandler>().resources.getString(R.string.direct_message),
                if (phoneName == null) on<ResourcesHandler>().resources.getString(R.string.talk) else on<ResourcesHandler>().resources.getString(R.string.talk_to, phoneName),
                on<ResourcesHandler>().resources.getString(R.string.send_message)
        ) { message ->
            if (message.isBlank()) {
                return@show
            }

            on<DisposableHandler>().add(on<ApiHandler>().sendMessage(phoneId, message).subscribe({ successResult ->
                if (!successResult.success) {
                    on<DefaultAlerts>().thatDidntWork()
                }
            }, { on<DefaultAlerts>().thatDidntWork() }))
        }
    }
}