package closer.vlllage.com.closer.handler.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.Val
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.subjects.PublishSubject

class PersistenceHandler constructor(private val on: On) : OnLifecycle {

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        Companion.changes.onNext(key)
    }

    private lateinit var sharedPreferences: SharedPreferences

    val changes = Companion.changes

    var appsToolbarOrder: List<ContentViewType>
        get() = sharedPreferences.getString(PREFERENCE_APPS_TOOLBAR_ORDER, null)?.split(",")?.mapNotNull {
            try {
                ContentViewType.valueOf(it)
            } catch (e: Exception) {
                null
            }
        } ?: listOf()
        @SuppressLint("ApplySharedPref")
        set(value) {
            sharedPreferences.edit().putString(PREFERENCE_APPS_TOOLBAR_ORDER, value.joinToString(",") { it.name }).commit()
        }

    var myStatus: String
        get() = sharedPreferences.getString(PREFERENCE_MY_STATUS, "")!!
        @SuppressLint("ApplySharedPref")
        set(status) {
            sharedPreferences.edit().putString(PREFERENCE_MY_STATUS, status).commit()
        }

    var myName: String
        get() = sharedPreferences.getString(PREFERENCE_MY_NAME, "")!!
        @SuppressLint("ApplySharedPref")
        set(name) {
            sharedPreferences.edit().putString(PREFERENCE_MY_NAME, name).commit()
        }

    var myPhoto: String
        get() = sharedPreferences.getString(PREFERENCE_MY_PHOTO, "")!!
        @SuppressLint("ApplySharedPref")
        set(name) {
            sharedPreferences.edit().putString(PREFERENCE_MY_PHOTO, name).commit()
        }

    var myActive: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_MY_ACTIVE, false)
        @SuppressLint("ApplySharedPref")
        set(active) {
            sharedPreferences.edit().putBoolean(PREFERENCE_MY_ACTIVE, active).commit()
        }

    var access: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_ACCESS, false)
        @SuppressLint("ApplySharedPref")
        set(active) {
            sharedPreferences.edit().putBoolean(PREFERENCE_ACCESS, active).commit()
        }

    var deviceToken: String?
        get() = sharedPreferences.getString(PREFERENCE_DEVICE_TOKEN, null)
        @SuppressLint("ApplySharedPref")
        set(deviceToken) {
            sharedPreferences.edit().putString(PREFERENCE_DEVICE_TOKEN, deviceToken).commit()
        }

    var phone: String?
        get() = sharedPreferences.getString(PREFERENCE_PHONE, null)
        @SuppressLint("ApplySharedPref")
        set(phone) {
            sharedPreferences.edit().putString(PREFERENCE_PHONE, phone).commit()
        }

    var isVerified: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_VERIFIED, false)
        @SuppressLint("ApplySharedPref")
        set(verified) {
            sharedPreferences.edit().putBoolean(PREFERENCE_VERIFIED, verified).commit()
        }

    var phoneId: String?
        get() = sharedPreferences.getString(PREFERENCE_PHONE_ID, null)
        @SuppressLint("ApplySharedPref")
        set(phoneId) {
            sharedPreferences.edit().putString(PREFERENCE_PHONE_ID, phoneId).commit()
        }

    var isNotificationsPaused: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, false)
        @SuppressLint("ApplySharedPref")
        set(isNotificationsPaused) {
            sharedPreferences.edit().putBoolean(PREFERENCE_NOTIFICATIONS_PAUSED, isNotificationsPaused).commit()
        }

    var privateMode: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_MY_PRIVATE_MODE, false)
        @SuppressLint("ApplySharedPref")
        set(privateMode) {
            sharedPreferences.edit().putBoolean(PREFERENCE_MY_PRIVATE_MODE, privateMode).commit()
        }

    var privateOnly: Boolean
        get() = sharedPreferences.getBoolean(PREFERENCE_MY_PRIVATE_ONLY, false)
        @SuppressLint("ApplySharedPref")
        set(privateOnly) {
            sharedPreferences.edit().putBoolean(PREFERENCE_MY_PRIVATE_ONLY, privateOnly).commit()
        }

    var lastMapCenter: LatLng?
        get() {
            val result = sharedPreferences.getString(PREFERENCE_LAST_MAP_CENTER, null)
                    ?: return null

            val latLng = result.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            return LatLng(
                    java.lang.Double.valueOf(latLng[0]),
                    java.lang.Double.valueOf(latLng[1])
            )
        }
        @SuppressLint("ApplySharedPref")
        set(latLng) {
            sharedPreferences.edit().putString(PREFERENCE_LAST_MAP_CENTER, latLng!!.latitude.toString() + "," + latLng.longitude).commit()
        }

    var lastFeedTab: FeedContent?
        get() = sharedPreferences.getString(PREFERENCE_LAST_FEED_CONTENT, null)?.let { on<Val>().valueOr(it, FeedContent.WELCOME) }
        @SuppressLint("ApplySharedPref")
        set(feedContent) {
            sharedPreferences.edit().putString(PREFERENCE_LAST_FEED_CONTENT, feedContent?.name).commit()
        }

    override fun on() {
        sharedPreferences = on<ApplicationHandler>().app.getSharedPreferences(
                SHARED_PREFERENCES, Context.MODE_PRIVATE
        )

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        val changes = PublishSubject.create<String>()

        const val SHARED_PREFERENCES = "closer.prefs"
        const val PREFERENCE_APPS_TOOLBAR_ORDER = "closer.apps.toolbar.order"
        const val PREFERENCE_MY_STATUS = "closer.me.status"
        const val PREFERENCE_MY_NAME = "closer.me.name"
        const val PREFERENCE_MY_ACTIVE = "closer.me.active"
        const val PREFERENCE_MY_PHOTO = "closer.me.photo"
        const val PREFERENCE_MY_PRIVATE_MODE = "closer.me.private-mode"
        const val PREFERENCE_MY_PRIVATE_ONLY = "closer.me.private-only"
        const val PREFERENCE_DEVICE_TOKEN = "closer.device-token"
        const val PREFERENCE_PHONE = "closer.phone"
        const val PREFERENCE_VERIFIED = "closer.verified"
        const val PREFERENCE_PHONE_ID = "closer.phone.id"
        const val PREFERENCE_NOTIFICATIONS_PAUSED = "closer.notifications.paused"
        const val PREFERENCE_LAST_MAP_CENTER = "closer.map.center"
        const val PREFERENCE_LAST_FEED_CONTENT = "closer.feed.content"
        const val PREFERENCE_ACCESS = "closer.access"
    }
}
