package closer.vlllage.com.closer.store

import closer.vlllage.com.closer.store.models.QuestFlow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter

class QuestFlowJsonConverter : PropertyConverter<QuestFlow?, String> {

    override fun convertToEntityProperty(databaseValue: String?): QuestFlow? {
        return if (databaseValue == null) null
        else gson.fromJson(databaseValue, object : TypeToken<QuestFlow?>() {}.type)

    }

    override fun convertToDatabaseValue(entityProperty: QuestFlow?): String? {
        return if (entityProperty == null) null
        else gson.toJson(entityProperty)
    }

    companion object {
        private val gson = Gson()
    }
}
