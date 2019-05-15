package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On

class ConnectionErrorHandler constructor(private val on: On) {

    var onConnectionErrorListener: (() -> Unit)? = null

    fun notifyConnectionError() {
        if (onConnectionErrorListener != null) {
            onConnectionErrorListener!!.invoke()
            return
        }

        on<DefaultAlerts>().syncError()
    }
}
