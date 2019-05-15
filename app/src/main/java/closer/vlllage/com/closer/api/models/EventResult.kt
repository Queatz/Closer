package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.Event
import com.google.gson.annotations.SerializedName
import java.util.*

class EventResult : ModelResult() {
    var geo: List<Double>? = null
    var name: String? = null
    var about: String? = null
    @SerializedName("public")
    private val isPublic: Boolean = false
    var startsAt: Date? = null
    var endsAt: Date? = null
    var cancelled: Boolean = false
    var groupId: String? = null
    var creator: String? = null

    companion object {

        fun from(eventResult: EventResult): Event {
            val event = Event()
            event.id = eventResult.id
            updateFrom(event, eventResult)
            return event
        }

        fun updateFrom(event: Event, eventResult: EventResult): Event {
            event.name = eventResult.name
            event.about = eventResult.about
            event.isPublic = eventResult.isPublic
            event.latitude = eventResult.geo!![0]
            event.longitude = eventResult.geo!![1]
            event.endsAt = eventResult.endsAt
            event.startsAt = eventResult.startsAt
            event.cancelled = eventResult.cancelled
            event.groupId = eventResult.groupId
            event.creator = eventResult.creator
            return event
        }
    }
}