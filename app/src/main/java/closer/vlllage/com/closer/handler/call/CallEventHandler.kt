package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import java.util.logging.Logger.getAnonymousLogger

class CallEventHandler(private val on: On) {
    fun handle(callEvent: CallEvent) {
        getAnonymousLogger().warning("CALL-XXX GETT ${callEvent.event} | ${callEvent.data}")

        if (callEvent.phone == on<PersistenceHandler>().phoneId) {
            return
        }

        when (callEvent.event) {
            "start" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onStart(callEvent)
            "offer" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onOffer(callEvent)
            "connect" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onConnect(callEvent)
            "answer" -> on<ApplicationHandler>().app.on<CallConnectionHandler>().onAnswer(callEvent)
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
