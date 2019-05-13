package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember

class StoreRefHandler : PoolMember() {

    private lateinit var store: Store

    public override fun onPoolInit() {
        this.store = Store(`$`(ApplicationHandler::class.java).app)
    }

    override fun onPoolEnd() {
        store.close()
    }

    fun get() = store
}
