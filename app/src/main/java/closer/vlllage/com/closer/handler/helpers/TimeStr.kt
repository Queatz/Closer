package closer.vlllage.com.closer.handler.helpers

import android.text.format.DateUtils.*
import closer.vlllage.com.closer.R
import com.queatz.on.On
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class TimeStr constructor(private val on: On) {

    var dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH)

    fun tiny(date: Date?): String {
        if (date == null) {
            return "-"
        }

        val millis = Date().time - date.time

        return when {
            millis < HOUR_IN_MILLIS -> on<ResourcesHandler>().resources.getString(R.string.date_tiny_minutes, (millis / MINUTE_IN_MILLIS).toString())
            millis < DAY_IN_MILLIS -> on<ResourcesHandler>().resources.getString(R.string.date_tiny_hours, (millis / HOUR_IN_MILLIS).toString())
            millis < WEEK_IN_MILLIS -> on<ResourcesHandler>().resources.getString(R.string.date_tiny_days, (millis / DAY_IN_MILLIS).toString())
            millis < YEAR_IN_MILLIS -> on<ResourcesHandler>().resources.getString(R.string.date_tiny_weeks, (millis / WEEK_IN_MILLIS).toString())
            else -> on<ResourcesHandler>().resources.getString(R.string.date_tiny_years, (millis / YEAR_IN_MILLIS).toString())
        }
    }

    fun approx(date: Date?): String {
        if (date == null) {
            return "-"
        }

        val millis = abs(Date().time - date.time)

        val result = when {
            millis < HOUR_IN_MILLIS -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.date_approx_minutes, (millis / MINUTE_IN_MILLIS).toInt(), (millis / MINUTE_IN_MILLIS).toString())
            millis < DAY_IN_MILLIS -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.date_approx_hours, (millis / HOUR_IN_MILLIS).toInt(), (millis / HOUR_IN_MILLIS).toString())
            millis < WEEK_IN_MILLIS -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.date_approx_days, (millis / DAY_IN_MILLIS).toInt(), (millis / DAY_IN_MILLIS).toString())
            millis < YEAR_IN_MILLIS -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.date_approx_weeks, (millis / WEEK_IN_MILLIS).toInt(), (millis / WEEK_IN_MILLIS).toString())
            else -> on<ResourcesHandler>().resources.getQuantityString(R.plurals.date_approx_years, (millis / YEAR_IN_MILLIS).toInt(), (millis / YEAR_IN_MILLIS).toString())
        }

        return if (Date().time < date.time)
            on<ResourcesHandler>().resources.getString(R.string.in_x, result)
        else result
    }

    fun pretty(date: Date?): String {
        if (date == null) {
            return "-"
        }

        if (Date().time - date.time < 5 * MINUTE_IN_MILLIS) {
            return on<ResourcesHandler>().resources.getString(R.string.just_now)
        }

        return if (isToday(date.time)) {
            getRelativeTimeSpanString(date.time).toString()
        } else getRelativeDateTimeString(
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

        return if (isToday(date.time)) {
            getRelativeTimeSpanString(date.time).toString()
        } else formatDateTime(
                on<ApplicationHandler>().app,
                date.time,
                0
        )

    }

    fun day(date: Date): String {
        return "${dateFormat.format(date)}${if (isToday(date.time)) " (${on<ResourcesHandler>().resources.getString(R.string.today)})" else ""}"
    }

    fun active(date: Date?) = on<ResourcesHandler>().resources.getString(R.string.active, approx(date))
    fun lastActive(date: Date?) = on<ResourcesHandler>().resources.getString(R.string.last_active, approx(date))
}
