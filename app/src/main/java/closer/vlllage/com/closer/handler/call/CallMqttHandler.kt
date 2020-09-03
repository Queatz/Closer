package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.mqtt.MqttHandler
import closer.vlllage.com.closer.handler.mqtt.events.CallMqttEvent
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.logging.Logger.getAnonymousLogger

class CallMqttHandler(private val on: On) {

    var token: String? = null
    var callDisposableGroup = on<DisposableHandler>().group()
    val ready = BehaviorSubject.createDefault(false)

    private val mqtt = on<ApplicationHandler>().app.on<MqttHandler>()

    fun send(event: String, payload: Any) = token?.let {
        getAnonymousLogger().warning("CALL-XXX SEND $event | $payload")
        mqtt.publish("call/$it", CallMqttEvent(CallEvent(
                on<PersistenceHandler>().phoneId!!,
                on<PersistenceHandler>().myName,
                event,
                on<JsonHandler>().to(payload)
        )))
        true
    } ?: let {
        on<DefaultAlerts>().thatDidntWork()
        false
    }

    fun newCall() = on<Val>().rndId().also { switchCall(it) }

    fun switchCall(token: String) {
        endActiveCall()

        this.token = token

        mqtt.subscribe("call/$token") {
            ready.onNext(true)
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
        ready.onNext(false)
        this.token?.let { mqtt.unsubscribe("call/$it") }
        callDisposableGroup.clear()
    }
}
