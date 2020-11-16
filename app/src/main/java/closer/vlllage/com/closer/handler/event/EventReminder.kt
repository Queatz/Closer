package closer.vlllage.com.closer.handler.event

data class EventReminder constructor(
    var position: EventReminderPosition = EventReminderPosition.Start,
    val offset: EventReminderOffset = EventReminderOffset(),
    val time: EventReminderTime = EventReminderTime(),
    var repeat: EventReminderRepeat? = null
)

data class EventReminderOffset constructor(
        var amount: Int = -1,
        var unit: String = "day"
)

data class EventReminderTime constructor(
        var hour: Int = 0,
        var minute: Int = 0
)

data class EventReminderRepeat constructor(
        var until: EventReminderPosition = EventReminderPosition.End,
        var hours: List<String>? = null,
        var days: List<String>? = null,
        var weeks: List<String>? = null,
        var months: List<String>? = null,
)

enum class EventReminderPosition {
    Start,
    End
}
