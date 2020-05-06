package closer.vlllage.com.closer.handler.phone

import com.queatz.on.On

class NameCacheHandler constructor(private val on: On) {

    companion object {
        private val cache = mutableMapOf<String, String>()
    }

    operator fun get(phoneId: String) = cache[phoneId]
    operator fun set(phoneId: String, value: String) = cache.put(phoneId, value)
}
