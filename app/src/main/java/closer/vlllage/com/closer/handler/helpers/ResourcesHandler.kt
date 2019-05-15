package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On

class ResourcesHandler constructor(private val on: On) {
    val resources get() = on<ApplicationHandler>().app.resources!!
}
