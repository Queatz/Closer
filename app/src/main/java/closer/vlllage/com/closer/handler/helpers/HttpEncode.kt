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

        try {
            return URLEncoder.encode(string, UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }

    }

    fun decode(string: String?): String? {
        if (string == null) {
            return null
        }

        try {
            return URLDecoder.decode(string, UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }

    }

    companion object {

        private val UTF_8 = "UTF-8"
    }
}
