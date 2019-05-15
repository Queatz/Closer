package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import java.text.SimpleDateFormat
import java.util.*

class DateFormatter constructor(private val on: On) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    fun format(date: Date): String {
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(date)
    }
}
