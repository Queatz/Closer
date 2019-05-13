package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

class HttpEncode : PoolMember() {

    fun encode(string: String?): String? {
        if (string == null) {
            return null
        }

        return try {
            URLEncoder.encode(string, UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }

    }

    fun decode(string: String?): String? {
        if (string == null) {
            return null
        }

        return try {
            URLDecoder.decode(string, UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }

    }

    companion object {
        private const val UTF_8 = "UTF-8"
    }
}
