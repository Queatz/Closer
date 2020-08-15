package closer.vlllage.com.closer.handler.map

import android.view.View
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import com.queatz.on.On

class NetworkConnectionViewHandler constructor(private val on: On) {

    private lateinit var connectionErrorView: View
    private val hideCallback = Runnable {
        hide()
    }

    fun attach(connectionErrorView: View) {
        this.connectionErrorView = connectionErrorView
        on<ConnectionErrorHandler>().onConnectionErrorListener = {
            show()
        }

        this.connectionErrorView.setOnClickListener { hide() }
    }

    fun hide() {
        connectionErrorView.visible = false
    }

    private fun show() {
        connectionErrorView.visible = true
        connectionErrorView.removeCallbacks(hideCallback)
        connectionErrorView.postDelayed(hideCallback, NETWORK_CONNECTION_ERROR_TIMEOUT_MS)
    }

    companion object {

        private const val NETWORK_CONNECTION_ERROR_TIMEOUT_MS: Long = 5000
    }
}
