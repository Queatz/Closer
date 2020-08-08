package closer.vlllage.com.closer.handler.call

import com.queatz.on.On

class CallEventHandler constructor(private val on: On) {
    fun handle(callEvent: CallEvent) {
        when (callEvent.event) {
            "start" -> on<CallConnectionHandler>().onStart(callEvent)
            "connect" -> on<CallConnectionHandler>().onConnect(callEvent)
            "accept" -> on<CallConnectionHandler>().onAccept(callEvent)
            "end" -> on<CallConnectionHandler>().onEnd(callEvent)
        }
    }
}

data class CallEvent constructor(
        val phone: String,
        val event: String,
        val data: String
)
