package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import com.queatz.on.OnLifecycle

class StoreRefHandler constructor(private val on: On) : OnLifecycle {

    private lateinit var store: Store

    override fun on() {
        this.store = Store(on<ApplicationHandler>().app)
    }

    override fun off() {
        store.close()
    }

    fun get() = store
}
