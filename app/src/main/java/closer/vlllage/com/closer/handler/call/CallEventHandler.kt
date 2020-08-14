package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class CallEventHandler constructor(private val on: On) {
    fun handle(callEvent: CallEvent) {
        when (callEvent.event) {
            "start" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onStart(callEvent)
            "connect" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onConnect(callEvent)
            "accept" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onAccept(callEvent)
            "end" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onEnd(callEvent)
        }
    }
}

data class CallEvent constructor(
        val phone: String,
        val phoneName: String? = null,
        val event: String,
        val data: String
)
