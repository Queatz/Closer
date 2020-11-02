package closer.vlllage.com.closer.handler.event

import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.setToEndOfDay
import closer.vlllage.com.closer.extensions.setToStartOfDay
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsHandler constructor(private val on: On) {
    private val timeFormatter = SimpleDateFormat("h:mma", Locale.US)

    fun formatEventDetails(event: Event): String {
        if (event.cancelled) {
            return on<ResourcesHandler>().resources.getString(R.string.cancelled)
        }

        timeFormatter.timeZone = TimeZone.getDefault()
        val startTime = timeFormatter.format(event.startsAt)
        val endTime = timeFormatter.format(event.endsAt)
        val day = DateUtils.getRelativeTimeSpanString(
                event.startsAt!!.time,
                Date().time,
                DAY_IN_MILLIS
        ).toString()

        val endDay = DateUtils.getRelativeTimeSpanString(
                event.endsAt!!.time,
                Date().time,
                DAY_IN_MILLIS
        ).toString()

        val now = Calendar.getInstance(TimeZone.getDefault()).time

        val endsAtCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            time = event.startsAt!!

            if (event.allDay) setToEndOfDay()
        }.time

        val startsAtCalendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            time = event.startsAt!!

            if (event.allDay) setToStartOfDay()
        }.time

        val isHappeningNow = now.after(startsAtCalendar) && now.before(endsAtCalendar)

        var eventTimeText = when {
            event.allDay -> if (day == endDay) day else on<ResourcesHandler>().resources.getString(R.string.event_start_end_day, day, endDay)
            day == endDay -> on<ResourcesHandler>().resources.getString(R.string.event_start_end_time, startTime, endTime, day)
            else -> on<ResourcesHandler>().resources.getString(R.string.event_start_end_time_multiday, startTime, day, endTime, endDay)
        }

        if (isHappeningNow) {
            eventTimeText = on<ResourcesHandler>().resources.getString(
                    if (event.allDay)
                        R.string.event_happening_all_day
                    else
                        R.string.event_happening_now, eventTimeText)
        }

        return if (event.about?.isBlank() == false) {
            on<ResourcesHandler>().resources
                    .getString(R.string.event_price_and_time, event.about, eventTimeText)
        } else {
            eventTimeText
        }
    }

    fun formatRelative(date: Date): String {
        return DateUtils.getRelativeTimeSpanString(
                date.time,
                Date().time,
                MINUTE_IN_MILLIS
        ).toString()
    }
}
