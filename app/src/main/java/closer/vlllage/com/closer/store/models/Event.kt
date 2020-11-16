package closer.vlllage.com.closer.store.models

import closer.vlllage.com.closer.handler.event.EventReminder
import closer.vlllage.com.closer.store.EventRemindersListJsonConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import java.util.*

@Entity
class Event : BaseObject() {
    var name: String? = null
    var about: String? = null
    var isPublic: Boolean = false
    var groupId: String? = null
    var creator: String? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var startsAt: Date? = null
    var endsAt: Date? = null
    var allDay: Boolean = false
    var cancelled: Boolean = false

    @Convert(converter = EventRemindersListJsonConverter::class, dbType = String::class)
    var reminders: List<EventReminder>? = null
}
