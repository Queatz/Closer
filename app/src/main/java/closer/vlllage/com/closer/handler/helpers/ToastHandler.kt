package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.StringRes
import android.widget.Toast

import closer.vlllage.com.closer.pool.PoolMember

class ToastHandler : PoolMember() {

    fun show(@StringRes message: Int) {
        Toast.makeText(`$`(ApplicationHandler::class.java).app, message, Toast.LENGTH_SHORT).show()
    }

    fun show(message: String) {
        Toast.makeText(`$`(ApplicationHandler::class.java).app, message, Toast.LENGTH_SHORT).show()
    }
}
