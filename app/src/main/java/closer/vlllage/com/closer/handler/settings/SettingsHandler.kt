package closer.vlllage.com.closer.handler.settings

import android.content.Context
import android.content.SharedPreferences

import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember

class SettingsHandler : PoolMember() {

    private var sharedPreferences: SharedPreferences? = null

    override fun onPoolInit() {
        sharedPreferences = `$`(ApplicationHandler::class.java).app.getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        )
    }

    operator fun get(userLocalSetting: UserLocalSetting): Boolean {
        return sharedPreferences!!.getBoolean(userLocalSetting.name, false)
    }

    operator fun set(userLocalSetting: UserLocalSetting, newValue: Boolean) {
        sharedPreferences!!.edit().putBoolean(userLocalSetting.name, newValue).apply()
    }

    companion object {
        private const val SHARED_PREFERENCES = "closer.user.settings"
    }
}
