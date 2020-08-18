package closer.vlllage.com.closer.handler.call

import com.queatz.on.On

class CallHandler(private val on: On) {
    fun startCall(phoneId: String) {
        on<CallActivityTransitionHandler>().show(null, phoneId)
    }

    fun onReceiveCall(phoneId: String, phoneName: String?) {
        on<CallActivityTransitionHandler>().show(null, phoneId, phoneName, incoming = true)
    }
}