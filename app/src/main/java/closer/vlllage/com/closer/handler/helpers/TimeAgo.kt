package closer.vlllage.com.closer.handler.helpers

import android.text.format.DateUtils.*
import com.queatz.on.On
import java.util.*

class TimeAgo constructor(private val on: On) {
    fun fifteenDaysAgo(): Date {
        val fifteenDaysAgo = Date()
        fifteenDaysAgo.time = fifteenDaysAgo.time - 15 * DAY_IN_MILLIS
        return fifteenDaysAgo
    }

    fun thirtySixHoursAgo(): Date {
        val thirtySixHoursAgo = Date()
        thirtySixHoursAgo.time = thirtySixHoursAgo.time - 36 * HOUR_IN_MILLIS
        return thirtySixHoursAgo
    }

    fun oneMonthAgo(): Date {
        val oneMonthAgo = Date()
        oneMonthAgo.time = oneMonthAgo.time - 30 * DAY_IN_MILLIS
        return oneMonthAgo
    }

    fun oneHourAgo(): Date {
        val oneHourAgo = Date()
        oneHourAgo.time = oneHourAgo.time - HOUR_IN_MILLIS
        return oneHourAgo
    }

    fun oneDayAgo(): Date {
        val oneDayAgo = Date()
        oneDayAgo.time = oneDayAgo.time - DAY_IN_MILLIS
        return oneDayAgo
    }

    fun daysAgo(days: Int = 1): Date {
        val oneDayAgo = Date()
        oneDayAgo.time = oneDayAgo.time - (DAY_IN_MILLIS * days)
        return oneDayAgo
    }

    fun weeksAgo(weeks: Int = 1): Date {
        val oneDayAgo = Date()
        oneDayAgo.time = oneDayAgo.time - (WEEK_IN_MILLIS * weeks)
        return oneDayAgo
    }

    fun minutesAgo(minutes: Int): Date {
        val oneDayAgo = Date()
        oneDayAgo.time = oneDayAgo.time - (MINUTE_IN_MILLIS * minutes)
        return oneDayAgo
    }

    fun fifteenMinutesAgo(): Date {
        val ago = Date()
        ago.time = ago.time - MINUTE_IN_MILLIS * 15
        return ago
    }

    fun startOfToday(offsetInDays: Int = 0) = Calendar.getInstance(TimeZone.getDefault()).apply {
        add(Calendar.DATE, offsetInDays)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}
