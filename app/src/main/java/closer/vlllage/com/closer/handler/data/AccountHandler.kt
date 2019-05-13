package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.PublishSubject

class AccountHandler : PoolMember() {

    val name get() = `$`(PersistenceHandler::class.java).myName
    val status get() = `$`(PersistenceHandler::class.java).myStatus
    val active get() = `$`(PersistenceHandler::class.java).myActive
    val phone get(): String {
        var phone = `$`(PersistenceHandler::class.java).phone

        if (phone == null) {
            phone = `$`(Val::class.java).rndId()
            `$`(PersistenceHandler::class.java).phone = phone
        }

        return phone
    }

    val privateMode get() = `$`(PersistenceHandler::class.java).privateMode

    fun updateGeo(latLng: LatLng) {
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_GEO, latLng))

        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhone(`$`(LatLngStr::class.java).from(latLng), null, null, null, null)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updateName(name: String) {
        `$`(PersistenceHandler::class.java).myName = name
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NAME, name))
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhone(null, name, null, null, null)
                .subscribe({ success -> }, { this.onError(it) }))

    }

    fun updatePhoto(photoUrl: String) {
        `$`(PersistenceHandler::class.java).myPhoto = photoUrl
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_PHOTO, photoUrl))
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhonePhoto(photoUrl)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updateStatus(status: String) {
        `$`(PersistenceHandler::class.java).myStatus = status
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_STATUS, status))
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhone(null, null, status, null, null)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updatePrivateMode(privateMode: Boolean) {
        `$`(PersistenceHandler::class.java).privateMode = privateMode
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NOTIFICATIONS, privateMode))
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhonePrivateMode(privateMode)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    private fun onError(throwable: Throwable) {
        throwable.printStackTrace()
        `$`(ConnectionErrorHandler::class.java).notifyConnectionError()
    }

    fun updateActive(active: Boolean) {
        `$`(PersistenceHandler::class.java).myActive = active
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_ACTIVE, active))
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhone(null, null, null, active, null)
                .subscribe({ success -> }, { this.onError(it) }))

        if (!active) {
            return
        }

        `$`(LocationHandler::class.java).getCurrentLocation { location -> updateGeo(LatLng(location.latitude, location.longitude)) }
    }

    fun updateDeviceToken(deviceToken: String) {
        `$`(PersistenceHandler::class.java).deviceToken = deviceToken
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).updatePhone(null, null, null, null, deviceToken)
                .subscribe({ createResult -> `$`(PersistenceHandler::class.java).phoneId = createResult.id }, { this.onError(it) }))
    }

    fun changes() = accountChanges

    class AccountChange(val prop: String, val value: Any)

    companion object {

        const val ACCOUNT_FIELD_STATUS = "status"
        const val ACCOUNT_FIELD_NAME = "name"
        const val ACCOUNT_FIELD_PHOTO = "photo"
        const val ACCOUNT_FIELD_GEO = "geo"
        const val ACCOUNT_FIELD_ACTIVE = "active"
        const val ACCOUNT_FIELD_NOTIFICATIONS = "notifications"

        private val accountChanges = PublishSubject.create<AccountChange>()
    }
}
