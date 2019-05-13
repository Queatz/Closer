package closer.vlllage.com.closer.handler.map

import android.view.View

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.pool.PoolMember

class NetworkConnectionViewHandler : PoolMember() {

    private var connectionErrorView: View? = null
    private val hideCallback = Runnable {
        if (connectionErrorView != null) {
            connectionErrorView!!.visibility = View.GONE
        }
    }

    fun attach(connectionErrorView: View) {
        this.connectionErrorView = connectionErrorView
        `$`(ConnectionErrorHandler::class.java).setOnConnectionErrorListener(object : ConnectionErrorHandler.OnConnectionErrorListener {
            override fun onConnectionError() {
                show()
            }
        })
    }

    private fun show() {
        connectionErrorView!!.visibility = View.VISIBLE
        connectionErrorView!!.removeCallbacks(hideCallback)
        connectionErrorView!!.postDelayed(hideCallback, NETWORK_CONNECTION_ERROR_TIMEOUT_MS)
    }

    companion object {

        private const val NETWORK_CONNECTION_ERROR_TIMEOUT_MS: Long = 5000
    }
}
