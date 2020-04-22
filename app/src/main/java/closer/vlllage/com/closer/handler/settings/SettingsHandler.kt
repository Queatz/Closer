package closer.vlllage.com.closer.handler.settings

import android.content.Context
import android.content.SharedPreferences
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class SettingsHandler constructor(private val on: On) : OnLifecycle {

    companion object {
        private const val SHARED_PREFERENCES = "closer.user.settings"
        private val changes = PublishSubject.create<SettingsChange>()
    }

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
        changes.onNext(SettingsChange(userLocalSetting, newValue))
    }

    fun observe(setting: UserLocalSetting): Observable<Boolean> = changes
            .filter { it.setting == setting }
            .map { it.value }
            .startWith(get(setting))
            .observeOn(AndroidSchedulers.mainThread())
}

data class SettingsChange constructor(
        val setting: UserLocalSetting,
        val value: Boolean
)