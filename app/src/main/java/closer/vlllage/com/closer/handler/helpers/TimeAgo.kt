package closer.vlllage.com.closer.handler.helpers

import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.HOUR_IN_MILLIS
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
}
