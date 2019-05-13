package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class ConnectionErrorHandler : PoolMember() {

    var onConnectionErrorListener: (() -> Unit)? = null

    fun notifyConnectionError() {
        if (onConnectionErrorListener != null) {
            onConnectionErrorListener!!.invoke()
            return
        }

        `$`(DefaultAlerts::class.java).syncError()
    }
}
