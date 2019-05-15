package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.Val
import com.queatz.on.On
import com.google.android.gms.maps.model.LatLng
import io.reactivex.subjects.PublishSubject

class AccountHandler constructor(private val on: On) {

    val name get() = on<PersistenceHandler>().myName
    val status get() = on<PersistenceHandler>().myStatus
    val active get() = on<PersistenceHandler>().myActive
    val phone get(): String {
        var phone = on<PersistenceHandler>().phone

        if (phone == null) {
            phone = on<Val>().rndId()
            on<PersistenceHandler>().phone = phone
        }

        return phone
    }

    val privateMode get() = on<PersistenceHandler>().privateMode

    fun updateGeo(latLng: LatLng) {
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_GEO, latLng))

        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(on<LatLngStr>().from(latLng), null, null, null, null)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updateName(name: String) {
        on<PersistenceHandler>().myName = name
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NAME, name))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(null, name, null, null, null)
                .subscribe({ success -> }, { this.onError(it) }))

    }

    fun updatePhoto(photoUrl: String) {
        on<PersistenceHandler>().myPhoto = photoUrl
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_PHOTO, photoUrl))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhonePhoto(photoUrl)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updateStatus(status: String) {
        on<PersistenceHandler>().myStatus = status
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_STATUS, status))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(null, null, status, null, null)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    fun updatePrivateMode(privateMode: Boolean) {
        on<PersistenceHandler>().privateMode = privateMode
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NOTIFICATIONS, privateMode))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhonePrivateMode(privateMode)
                .subscribe({ success -> }, { this.onError(it) }))
    }

    private fun onError(throwable: Throwable) {
        throwable.printStackTrace()
        on<ConnectionErrorHandler>().notifyConnectionError()
    }

    fun updateActive(active: Boolean) {
        on<PersistenceHandler>().myActive = active
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_ACTIVE, active))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(null, null, null, active, null)
                .subscribe({ success -> }, { this.onError(it) }))

        if (!active) {
            return
        }

        on<LocationHandler>().getCurrentLocation { location -> updateGeo(LatLng(location.latitude, location.longitude)) }
    }

    fun updateDeviceToken(deviceToken: String) {
        on<PersistenceHandler>().deviceToken = deviceToken
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(null, null, null, null, deviceToken)
                .subscribe({ createResult -> on<PersistenceHandler>().phoneId = createResult.id }, { this.onError(it) }))
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
