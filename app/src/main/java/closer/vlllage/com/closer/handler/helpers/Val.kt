package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import java.util.*

class Val constructor(private val on: On) {
    fun rndId(): String {
        val random = Random()
        return random.nextLong().toString() +
                random.nextLong() +
                random.nextLong()
    }

    fun trimmed(string: String?): String {
        return of(string?.trim(), "")
    }

    fun of(string: String?, stringWhenEmpty: String): String {
        return if (string.isNullOrBlank()) stringWhenEmpty else string
    }
}
