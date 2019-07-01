package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.store.models.Phone
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class PhoneDetailsHandler constructor(private val on: On) {

    fun detailsOf(phone: Phone): String {
        val groups = if (
                phone.updated!!.after(on<TimeAgo>().oneHourAgo()) &&
                phone.latitude != null && phone.longitude != null
        ) on<ProximityHandler>().findGroupsNear(LatLng(
                phone.latitude!!,
                phone.longitude!!
        )) else listOf()

        val groupName = groups.firstOrNull()?.name
        val isAt = groups.firstOrNull()?.let { on<DistanceHandler>().isPhoneNearGroup(it, phone) } ?: false

        return (if (phone.occupation != null) "\n" + phone.occupation else "") +
                (if (groupName != null) "\n" + (if (isAt)
                    on<ResourcesHandler>().resources.getString(R.string.at)
                else
                    on<ResourcesHandler>().resources.getString(R.string.near)
                ) + " " + groupName else "") +
                (if (phone.occupation != null || groupName != null) "\n" else "")
    }
}