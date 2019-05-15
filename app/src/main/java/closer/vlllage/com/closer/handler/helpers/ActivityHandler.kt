package closer.vlllage.com.closer.handler.helpers

import android.app.Activity

import com.queatz.on.On

class ActivityHandler constructor(private val on: On) {

    var activity: Activity? = null
    set(value) {
        if (this.activity != null) {
            throw IllegalStateException("Cannot set Activity twice! Use another on.")
        }
        field = value
    }

    val isPresent: Boolean
        get() = this.activity != null && !this.activity!!.isDestroyed
}
