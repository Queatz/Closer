package closer.vlllage.com.closer.handler.event

import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Event
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsHandler : PoolMember() {
    private val timeFormatter = SimpleDateFormat("h:mma", Locale.US)

    fun formatEventDetails(event: Event): String {
        if (event.cancelled) {
            return `$`(ResourcesHandler::class.java).resources.getString(R.string.cancelled)
        }

        timeFormatter.timeZone = TimeZone.getDefault()
        val startTime = timeFormatter.format(event.startsAt)
        val endTime = timeFormatter.format(event.endsAt)
        val day = DateUtils.getRelativeTimeSpanString(
                event.startsAt!!.time,
                Date().time,
                DAY_IN_MILLIS
        ).toString()

        val now = Calendar.getInstance().time
        val isHappeningNow = now.after(event.startsAt) && now.before(event.endsAt)

        var eventTimeText = `$`(ResourcesHandler::class.java).resources
                .getString(R.string.event_start_end_time, startTime, endTime, day)

        if (isHappeningNow) {
            eventTimeText = `$`(ResourcesHandler::class.java).resources.getString(R.string.event_happening_now, eventTimeText)
        }

        return if (event.about?.isBlank() == false) {
            `$`(ResourcesHandler::class.java).resources
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
