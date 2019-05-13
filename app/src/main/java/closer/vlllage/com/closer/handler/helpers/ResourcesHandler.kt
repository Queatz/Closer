package closer.vlllage.com.closer.handler.helpers

import android.content.res.Resources

import closer.vlllage.com.closer.pool.PoolMember

class ResourcesHandler : PoolMember() {
    val resources: Resources
        get() = `$`(ApplicationHandler::class.java).app!!.resources
}
