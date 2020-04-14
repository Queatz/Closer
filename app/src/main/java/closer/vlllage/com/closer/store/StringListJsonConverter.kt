package closer.vlllage.com.closer.store

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter


class StringListJsonConverter : PropertyConverter<List<String>, String> {

    override fun convertToEntityProperty(databaseValue: String?): List<String>? {
        return if (databaseValue == null) {
            null
        } else gson.fromJson<List<String>>(databaseValue, object : TypeToken<List<String?>>() {

        }.type)

    }

    override fun convertToDatabaseValue(entityProperty: List<String>): String {
        return gson.toJson(entityProperty)
    }

    companion object {
        private val gson = Gson()
    }
}
