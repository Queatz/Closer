package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class ConnectionErrorHandler : PoolMember() {

    private var onConnectionErrorListener: OnConnectionErrorListener? = null

    fun setOnConnectionErrorListener(onConnectionErrorListener: OnConnectionErrorListener) {
        this.onConnectionErrorListener = onConnectionErrorListener
    }

    fun notifyConnectionError() {
        if (onConnectionErrorListener != null) {
            onConnectionErrorListener!!.onConnectionError()
            return
        }

        `$`(DefaultAlerts::class.java).syncError()
    }

    interface OnConnectionErrorListener {
        fun onConnectionError()
    }
}
