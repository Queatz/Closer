package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.handler.event.EventReminder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter

class EventRemindersListJsonConverter : PropertyConverter<List<EventReminder>, String> {

    override fun convertToEntityProperty(databaseValue: String?): List<EventReminder> {
        return if (databaseValue == null) listOf()
        else gson.fromJson(databaseValue, object : TypeToken<List<EventReminder>>() {}.type)

    }

    override fun convertToDatabaseValue(entityProperty: List<EventReminder>): String {
        return gson.toJson(entityProperty)
    }

    companion object {
        private val gson = Gson()
    }
}
