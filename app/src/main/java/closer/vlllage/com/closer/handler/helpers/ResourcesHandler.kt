package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember

class ResourcesHandler : PoolMember() {
    val resources get() = `$`(ApplicationHandler::class.java).app.resources!!
}
