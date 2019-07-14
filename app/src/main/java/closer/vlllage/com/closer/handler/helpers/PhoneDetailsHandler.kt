package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.store.models.Phone
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class PhoneDetailsHandler constructor(private val on: On) {

    fun detailsOf(phone: Phone, small: Boolean = false): String {
        val groups = if (
                !small &&
                phone.updated!!.after(on<TimeAgo>().oneHourAgo()) &&
                phone.latitude != null && phone.longitude != null
        ) on<ProximityHandler>().findGroupsNear(LatLng(
                phone.latitude!!,
                phone.longitude!!
        )) else listOf()

        val groupName = groups.firstOrNull()?.name
        val isAt = groups.firstOrNull()?.let { on<DistanceHandler>().isPhoneNearGroup(it, phone) } ?: false
        val occupation = if (small) null else phone.occupation

        return (if (occupation != null) "\n" + occupation else "") +
                (if (groupName != null) "\n" + (if (isAt)
                    on<ResourcesHandler>().resources.getString(R.string.at)
                else
                    on<ResourcesHandler>().resources.getString(R.string.near)
                ) + " " + groupName else "") +
                (if (occupation != null || groupName != null) "\n" else "")
    }
}