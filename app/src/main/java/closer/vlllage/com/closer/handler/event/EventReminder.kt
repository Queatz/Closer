package closer.vlllage.com.closer.handler.event

import com.google.gson.annotations.SerializedName

data class EventReminder constructor(
    var position: EventReminderPosition = EventReminderPosition.Start,
    val offset: EventReminderOffset = EventReminderOffset(),
    val time: EventReminderTime = EventReminderTime(),
    var repeat: EventReminderRepeat? = null
)

data class EventReminderOffset constructor(
        var amount: Int = 0,
        var unit: EventReminderOffsetUnit = EventReminderOffsetUnit.Minute
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

enum class EventReminderOffsetUnit {
    @SerializedName("minute") Minute,
    @SerializedName("hour") Hour,
    @SerializedName("day") Day,
    @SerializedName("week") Week,
    @SerializedName("month") Month,
}

enum class EventReminderPosition {
    @SerializedName("start") Start,
    @SerializedName("end") End
}
