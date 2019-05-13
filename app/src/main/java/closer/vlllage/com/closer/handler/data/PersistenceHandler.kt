package closer.vlllage.com.closer.handler.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.model.LatLng

class PersistenceHandler : PoolMember() {

    private lateinit var sharedPreferences: SharedPreferences

    var myStatus: String
        get() = sharedPreferences!!.getString(PREFERENCE_MY_STATUS, "")
        @SuppressLint("ApplySharedPref")
        set(status) {
            sharedPreferences!!.edit().putString(PREFERENCE_MY_STATUS, status).commit()
        }

    var myName: String
        get() = sharedPreferences!!.getString(PREFERENCE_MY_NAME, "")
        @SuppressLint("ApplySharedPref")
        set(name) {
            sharedPreferences!!.edit().putString(PREFERENCE_MY_NAME, name).commit()
        }

    var myPhoto: String
        get() = sharedPreferences!!.getString(PREFERENCE_MY_PHOTO, "")
        @SuppressLint("ApplySharedPref")
        set(name) {
            sharedPreferences!!.edit().putString(PREFERENCE_MY_PHOTO, name).commit()
        }

    var myActive: Boolean
        get() = sharedPreferences!!.getBoolean(PREFERENCE_MY_ACTIVE, false)
        @SuppressLint("ApplySharedPref")
        set(active) {
            sharedPreferences!!.edit().putBoolean(PREFERENCE_MY_ACTIVE, active).commit()
        }

    var deviceToken: String?
        get() = sharedPreferences!!.getString(PREFERENCE_DEVICE_TOKEN, null)
        @SuppressLint("ApplySharedPref")
        set(deviceToken) {
            sharedPreferences!!.edit().putString(PREFERENCE_DEVICE_TOKEN, deviceToken).commit()
        }

    var phone: String?
        get() = sharedPreferences!!.getString(PREFERENCE_PHONE, null)
        @SuppressLint("ApplySharedPref")
        set(phone) {
            sharedPreferences!!.edit().putString(PREFERENCE_PHONE, phone).commit()
        }

    var isVerified: Boolean
        get() = sharedPreferences!!.getBoolean(PREFERENCE_VERIFIED, false)
        @SuppressLint("ApplySharedPref")
        set(verified) {
            sharedPreferences!!.edit().putBoolean(PREFERENCE_VERIFIED, verified).commit()
        }

    var phoneId: String?
        get() = sharedPreferences!!.getString(PREFERENCE_PHONE_ID, null)
        @SuppressLint("ApplySharedPref")
        set(phoneId) {
            sharedPreferences!!.edit().putString(PREFERENCE_PHONE_ID, phoneId).commit()
        }

    var isHelpHidden: Boolean
        get() = sharedPreferences!!.getBoolean(PREFERENCE_HELP_IS_HIDDEN, false)
        @SuppressLint("ApplySharedPref")
        set(helpIsHidden) {
            sharedPreferences!!.edit().putBoolean(PREFERENCE_HELP_IS_HIDDEN, helpIsHidden).commit()
        }

    var isNotificationsPaused: Boolean
        get() = sharedPreferences!!.getBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, false)
        @SuppressLint("ApplySharedPref")
        set(isNotificationsPaused) {
            sharedPreferences!!.edit().putBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, isNotificationsPaused).commit()
        }

    var privateMode: Boolean
        get() = sharedPreferences!!.getBoolean(PREFERENCE_MY_PRIVATE_MODE, false)
        @SuppressLint("ApplySharedPref")
        set(privateMode) {
            sharedPreferences!!.edit().putBoolean(PREFERENCE_MY_PRIVATE_MODE, privateMode).commit()
        }

    var lastMapCenter: LatLng?
        get() {
            val result = sharedPreferences!!.getString(PREFERENCE_LAST_MAP_CENTER, null)
                    ?: return null

            val latLng = result.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return LatLng(
                    java.lang.Double.valueOf(latLng[0]),
                    java.lang.Double.valueOf(latLng[1])
            )
        }
        @SuppressLint("ApplySharedPref")
        set(latLng) {
            sharedPreferences!!.edit().putString(PREFERENCE_LAST_MAP_CENTER, latLng!!.latitude.toString() + "," + latLng!!.longitude).commit()
        }

    override fun onPoolInit() {
        sharedPreferences = `$`(ApplicationHandler::class.java).app.getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        )
    }

    companion object {

        private const val SHARED_PREFERENCES = "closer.prefs"
        private const val PREFERENCE_MY_STATUS = "closer.me.status"
        private const val PREFERENCE_MY_NAME = "closer.me.name"
        private const val PREFERENCE_MY_ACTIVE = "closer.me.active"
        private const val PREFERENCE_MY_PHOTO = "closer.me.photo"
        private const val PREFERENCE_MY_PRIVATE_MODE = "closer.me.private-mode"
        private const val PREFERENCE_DEVICE_TOKEN = "closer.device-token"
        private const val PREFERENCE_PHONE = "closer.phone"
        private const val PREFERENCE_VERIFIED = "closer.verified"
        private const val PREFERENCE_PHONE_ID = "closer.phone.id"
        private const val PREFERENCE_HELP_IS_HIDDEN = "closer.state.help-is-hidden"
        private const val PREFERENCE_NOTIFICATIONS_PAUSED = "closer.notifications.paused"
        private const val PREFERENCE_LAST_MAP_CENTER = "closer.map.center"
    }
}
