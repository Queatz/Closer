package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.store.models.ReactionCount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter

class ReactionCountListJsonConverter : PropertyConverter<List<ReactionCount>, String> {

    override fun convertToEntityProperty(databaseValue: String?): List<ReactionCount>? {
        return if (databaseValue == null) {
            null
        } else gson.fromJson<List<ReactionCount>>(databaseValue, object : TypeToken<List<ReactionCount>>() {

        }.type)

    }

    override fun convertToDatabaseValue(entityProperty: List<ReactionCount>): String {
        return gson.toJson(entityProperty)
    }

    companion object {
        private val gson = Gson()
    }
}
