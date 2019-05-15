package closer.vlllage.com.closer.handler.helpers

import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.WEEK_IN_MILLIS
import closer.vlllage.com.closer.R
import com.queatz.on.On
import java.util.*

class TimeStr constructor(private val on: On) {

    fun pretty(date: Date?): String {
        if (date == null) {
            return "-"
        }

        if (Date().time - date.time < 5 * MINUTE_IN_MILLIS) {
            return on<ResourcesHandler>().resources.getString(R.string.just_now)
        }

        return if (DateUtils.isToday(date.time)) {
            DateUtils.getRelativeTimeSpanString(date.time).toString()
        } else DateUtils.getRelativeDateTimeString(
                on<ApplicationHandler>().app,
                date.time,
                MINUTE_IN_MILLIS,
                WEEK_IN_MILLIS,
                0
        ).toString()

    }

    fun prettyDate(date: Date?): String {
        if (date == null) {
            return "-"
        }

        return if (DateUtils.isToday(date.time)) {
            DateUtils.getRelativeTimeSpanString(date.time).toString()
        } else DateUtils.formatDateTime(
                on<ApplicationHandler>().app,
                date.time,
                0
        )

    }
}