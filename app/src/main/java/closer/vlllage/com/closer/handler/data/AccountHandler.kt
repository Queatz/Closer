package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.Val
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
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
    val privateOnly get() = on<PersistenceHandler>().privateOnly

    fun updatePhone(token: String) {
        on<PersistenceHandler>().phone = token
        on<ApiHandler>().setAuthorization(on<AccountHandler>().phone)
        on<RefreshHandler>().refreshAll()
    }

    fun updateGeo(latLng: LatLng) {
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_GEO, latLng))

        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(latLng = on<LatLngStr>().from(latLng))
                .subscribe({}, { this.onError(it) }))
    }

    fun updateName(name: String) {
        on<PersistenceHandler>().myName = name
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NAME, name))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(name = name)
                .subscribe({}, { this.onError(it) }))

    }

    fun updatePhoto(photoUrl: String) {
        on<PersistenceHandler>().myPhoto = photoUrl
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_PHOTO, photoUrl))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhonePhoto(photoUrl)
                .subscribe({}, { this.onError(it) }))
    }

    fun updateStatus(status: String) {
        on<PersistenceHandler>().myStatus = status
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_STATUS, status))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(status = status)
                .subscribe({}, { this.onError(it) }))
    }

    fun updatePrivateMode(privateMode: Boolean) {
        on<PersistenceHandler>().privateMode = privateMode
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_NOTIFICATIONS, privateMode))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhonePrivateMode(privateMode)
                .subscribe({}, { this.onError(it) }))
    }

    fun updatePrivateOnly(privateOnly: Boolean) {
        on<PersistenceHandler>().privateOnly = privateOnly
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_PRIVATE, privateOnly))
    }

    private fun onError(throwable: Throwable) {
        throwable.printStackTrace()
        on<ConnectionErrorHandler>().notifyConnectionError()
    }

    fun updateActive(active: Boolean) {
        on<PersistenceHandler>().myActive = active
        accountChanges.onNext(AccountChange(ACCOUNT_FIELD_ACTIVE, active))
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(active = active)
                .subscribe({}, { this.onError(it) }))

        if (!active) {
            return
        }

        on<LocationHandler>().getCurrentLocation { location -> updateGeo(LatLng(location.latitude, location.longitude)) }
    }

    fun updateDeviceToken(deviceToken: String) {
        on<PersistenceHandler>().deviceToken = deviceToken
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(deviceToken = deviceToken)
                .subscribe({ createResult -> on<PersistenceHandler>().phoneId = createResult.id }, { this.onError(it) }))
    }

    fun updateAbout(introduction: String? = null, offtime: String? = null, occupation: String? = null, history: String? = null, callback: (() -> Unit)? = null) {
        on<DisposableHandler>().add(on<ApiHandler>().updatePhone(
                introduction = introduction,
                offtime = offtime,
                occupation = occupation,
                history = history
        ).subscribe({
            callback?.invoke()
        }, { this.onError(it) }))
    }

    fun changes(prop: String? = null) = accountChanges.let { changes ->
        prop?.let { prop -> changes.startWith(AccountChange(prop, get(prop))).filter { it.prop == prop } } ?: changes
    }

    fun get(prop: String): Any? = when (prop) {
        ACCOUNT_FIELD_STATUS -> status
        ACCOUNT_FIELD_NAME -> name
        ACCOUNT_FIELD_PHOTO -> on<PersistenceHandler>().myPhoto
        ACCOUNT_FIELD_GEO -> on<LocationHandler>().lastKnownLocation?.let { LatLng(it.latitude, it.latitude) }
        ACCOUNT_FIELD_ACTIVE -> active
        ACCOUNT_FIELD_NOTIFICATIONS -> privateMode
        ACCOUNT_FIELD_PRIVATE -> privateOnly
        else -> null
    }

    class AccountChange(val prop: String = "", val value: Any? = null)

    companion object {

        const val ACCOUNT_FIELD_STATUS = "status"
        const val ACCOUNT_FIELD_NAME = "name"
        const val ACCOUNT_FIELD_PHOTO = "photo"
        const val ACCOUNT_FIELD_GEO = "geo"
        const val ACCOUNT_FIELD_ACTIVE = "active"
        const val ACCOUNT_FIELD_NOTIFICATIONS = "notifications"
        const val ACCOUNT_FIELD_PRIVATE = "private"

        private val accountChanges = PublishSubject.create<AccountChange>()
    }
}
