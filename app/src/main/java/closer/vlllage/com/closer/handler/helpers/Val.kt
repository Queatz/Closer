package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.common.util.Strings.isEmptyOrWhitespace
import java.util.*

class Val : PoolMember() {
    fun rndId(): String {
        val random = Random()
        return java.lang.Long.toString(random.nextLong()) +
                random.nextLong() +
                random.nextLong()
    }

    fun of(string: String?): String {
        return string?.trim { it <= ' ' } ?: ""
    }

    fun of(string: String?, stringWhenEmpty: String): String {
        return if (isEmpty(string)) stringWhenEmpty else string!!
    }

    fun isEmpty(string: String?): Boolean {
        return isEmptyOrWhitespace(string)
    }
}
