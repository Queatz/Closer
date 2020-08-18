package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.mqtt.MqttHandler
import closer.vlllage.com.closer.handler.mqtt.events.CallMqttEvent
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers

class CallMqttHandler(private val on: On) {

    var token: String? = null
    var callDisposableGroup = on<DisposableHandler>().group()

    private val mqtt = on<ApplicationHandler>().app.on<MqttHandler>()

    fun send(event: String, payload: Any) = token?.let {
        mqtt.publish("call/$it", CallMqttEvent(CallEvent(
                on<PersistenceHandler>().phoneId!!,
                on<PersistenceHandler>().myName,
                event,
                on<JsonHandler>().to(payload)
        )))
        true
    } ?: false

    fun newCall() = on<Val>().rndId().also { switchCall(it, false) }

    fun switchCall(token: String, sendReady: Boolean = true) {
        endActiveCall()

        this.token = token

        mqtt.subscribe("call/$token") {
            if (sendReady) send("ready", "")
        }

        mqtt.events("call/$token", CallMqttEvent::class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
            on<CallEventHandler>().handle(it.event!!)
        }, {
            on<DefaultAlerts>().syncError()
        }).also {
            callDisposableGroup.add(it)
        }
    }

    fun endActiveCall() {
        this.token?.let { mqtt.unsubscribe("call/$it") }
        callDisposableGroup.clear()
    }
}
