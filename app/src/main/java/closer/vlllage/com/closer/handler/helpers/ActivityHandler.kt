package closer.vlllage.com.closer.handler.helpers

import android.app.Activity

import closer.vlllage.com.closer.pool.PoolMember

class ActivityHandler : PoolMember() {

    var activity: Activity? = null
    set(value) {
        if (this.activity != null) {
            throw IllegalStateException("Cannot set Activity twice! Use another pool.")
        }
        field = value
    }

    val isPresent: Boolean
        get() = this.activity != null && !this.activity!!.isDestroyed
}
