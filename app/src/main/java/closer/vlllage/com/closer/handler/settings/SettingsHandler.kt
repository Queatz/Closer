package closer.vlllage.com.closer.handler.settings

import android.content.Context
import android.content.SharedPreferences

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import com.queatz.on.OnLifecycle

class SettingsHandler constructor(private val on: On) : OnLifecycle {

    private lateinit var sharedPreferences: SharedPreferences

    override fun on() {
        sharedPreferences = on<ApplicationHandler>().app.getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        )
    }

    operator fun get(userLocalSetting: UserLocalSetting): Boolean {
        return sharedPreferences.getBoolean(userLocalSetting.name, false)
    }

    operator fun set(userLocalSetting: UserLocalSetting, newValue: Boolean) {
        sharedPreferences.edit().putBoolean(userLocalSetting.name, newValue).apply()
    }

    companion object {
        private const val SHARED_PREFERENCES = "closer.user.settings"
    }
}
