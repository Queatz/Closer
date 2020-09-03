package closer.vlllage.com.closer.handler.call

import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On
import io.reactivex.Observable
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class CallStateHandler(private val on: On) {

    private val dispose = on<DisposableHandler>().group()

    private val bothReady = on<CallConnectionHandler>().remoteReady.switchMap { remoteReady ->
        on<CallMqttHandler>().ready.map { remoteReady && it }
    }

    fun onOutgoingCall(token: String) {
        ensure(on<CallMqttHandler>().ready) {
            on<CallConnectionHandler>().sendPushNotification("start", StartCallEvent(token))
        }
    }

    fun onIncomingCall() {
    }

    fun onIceCandidate(iceCandidate: IceCandidate) {
        ensure(bothReady) {
            send("connect", iceCandidate)
        }
    }

    fun onCreateAnswer(sessionDescription: SessionDescription) {
        ensure(on<CallMqttHandler>().ready) {
            send("ready", sessionDescription)
        }
    }

    fun onReady(localDescription: SessionDescription) {
        ensure(bothReady) {
            send("accept", localDescription)
        }
    }

    private fun send(event: String, payload: Any) {
        on<CallMqttHandler>().send(event, payload)
    }

    fun end() {
        dispose.clear()
    }

    private fun ensure(state: Observable<Boolean>, block: () -> Unit) = state.filter { it }
            .map { Unit }
            .take(1)
            .subscribe { block() }
            .also { dispose.add(it) }
}
