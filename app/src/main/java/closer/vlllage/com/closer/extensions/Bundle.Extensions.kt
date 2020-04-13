package closer.vlllage.com.closer.extensions

import android.os.Bundle
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun Bundle.toJson(json: JsonHandler): String {
    return json.to(JsonObject().let { json ->
        keySet().forEach {
            val value = this@toJson[it]

            when (value) {
                is String -> json.add(it, JsonPrimitive(value))
                is Number -> json.add(it, JsonPrimitive(value))
                is Boolean -> json.add(it, JsonPrimitive(value))
            }
        }

        json
    })
}


fun Bundle.fromJson(json: JsonHandler, string: String): Bundle {
    json.from(string, JsonObject::class.java).entrySet().forEach {
        val value = it.value.asJsonPrimitive
        when {
            value.isString -> putString(it.key, it.value.asString)
            value.isBoolean -> putBoolean(it.key, it.value.asBoolean)
            value.isNumber -> putInt(it.key, it.value.asInt)
        }
    }

    return this
}

