package closer.vlllage.com.closer.handler.group

import com.queatz.on.On

class MatchHandler constructor(private val on: On) {

    var active: Boolean = false
        private set

    fun activate() {
        active = true
    }
}
