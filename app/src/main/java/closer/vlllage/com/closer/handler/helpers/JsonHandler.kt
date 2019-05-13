package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.pool.PoolMember
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement

class JsonHandler : PoolMember() {

    private val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .create()

    fun <T> from(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    fun to(obj: Any): String {
        return gson.toJson(obj)
    }

    fun toJsonTree(obj: Any): JsonElement {
        return gson.toJsonTree(obj)
    }

    fun <T> from(jsonElement: JsonElement, clazz: Class<T>): T {
        return gson.fromJson(jsonElement, clazz)
    }
}
