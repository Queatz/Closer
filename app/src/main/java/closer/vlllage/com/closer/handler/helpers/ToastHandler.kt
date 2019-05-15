package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.StringRes
import android.widget.Toast

import com.queatz.on.On

class ToastHandler constructor(private val on: On) {

    fun show(@StringRes message: Int) {
        Toast.makeText(on<ApplicationHandler>().app, message, Toast.LENGTH_SHORT).show()
    }

    fun show(message: String) {
        Toast.makeText(on<ApplicationHandler>().app, message, Toast.LENGTH_SHORT).show()
    }
}
