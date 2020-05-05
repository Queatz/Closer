package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.StringRes
import android.widget.Toast

import com.queatz.on.On

class ToastHandler constructor(private val on: On) {

    fun show(@StringRes message: Int, long: Boolean = false) {
        Toast.makeText(on<ApplicationHandler>().app, message, duration(long)).show()
    }

    fun show(message: String, long: Boolean = false) {
        Toast.makeText(on<ApplicationHandler>().app, message, duration(long)).show()
    }

    private fun duration(long: Boolean) = when (long) {
        true -> Toast.LENGTH_LONG
        false -> Toast.LENGTH_SHORT
    }
}
