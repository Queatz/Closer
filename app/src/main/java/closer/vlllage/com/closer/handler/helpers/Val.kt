package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import com.google.android.gms.common.util.Strings.isEmptyOrWhitespace
import java.util.*

class Val constructor(private val on: On) {
    fun rndId(): String {
        val random = Random()
        return random.nextLong().toString() +
                random.nextLong() +
                random.nextLong()
    }

    fun of(string: String?): String {
        return string?.trim() ?: ""
    }

    fun of(string: String?, stringWhenEmpty: String): String {
        return if (isEmpty(string)) stringWhenEmpty else string!!
    }

    fun isEmpty(string: String?): Boolean {
        return isEmptyOrWhitespace(string)
    }
}
