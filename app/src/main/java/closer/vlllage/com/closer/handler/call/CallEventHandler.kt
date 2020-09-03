package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import java.util.logging.Logger.getAnonymousLogger

class CallEventHandler(private val on: On) {
    fun handle(callEvent: CallEvent) {
        getAnonymousLogger().warning("CALL-XXX GETT ${callEvent.event} | ${callEvent.data}")
        when (callEvent.event) {
            "start" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onStart(callEvent)
            "ready" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onReady(callEvent)
            "connect" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onConnect(callEvent)
            "accept" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onAccept(callEvent)
            "end" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onEnd(callEvent)
        }
    }
}

data class CallEvent(
        val phone: String,
        val phoneName: String? = null,
        val event: String,
        val data: String
)
