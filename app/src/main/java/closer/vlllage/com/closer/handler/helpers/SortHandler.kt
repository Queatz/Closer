package closer.vlllage.com.closer.handler.helpers

import android.location.Location.distanceBetween
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import java.util.*

class SortHandler constructor(private val on: On) {

    @JvmOverloads
    fun sortGroups(privateFirst: Boolean = true): Comparator<Group> {
        return Comparator { group, groupOther ->
            if (!on<DistanceHandler>().isPhoneNearGroup(group) && on<DistanceHandler>().isPhoneNearGroup(groupOther))
                1
            else if (!on<DistanceHandler>().isPhoneNearGroup(groupOther) && on<DistanceHandler>().isPhoneNearGroup(group))
                -1
            else if (privateFirst && group.isPublic && !groupOther.isPublic)
                1
            else if (privateFirst && groupOther.isPublic && !group.isPublic)
                -1
            else if (group.updated == null || groupOther.updated == null)
                0
            else
                groupOther.updated!!.compareTo(group.updated)
        }
    }

    fun sortGroupMessages(useReactions: Boolean = false): Comparator<GroupMessage> {
        return Comparator { groupMessage, groupMessageOther ->

            if (useReactions) {
                val reactions = groupMessage.reactions.map { it.count }.sum()
                val reactionsOther = groupMessageOther.reactions.map { it.count }.sum()

                if (reactions < reactionsOther) return@Comparator 1
                if (reactions > reactionsOther) return@Comparator -1
            }

            (groupMessageOther.created ?: Date(0)).compareTo(groupMessage.created ?: Date(0))
        }
    }

    fun sortPhones(): Comparator<Phone> {
        return Comparator { phone, phoneOther -> (phoneOther.updated ?: Date(0)).compareTo(phone.updated ?: Date(0)) }
    }

    fun sortGroupContacts(): Comparator<GroupContact> {
        return Comparator { groupContact, groupContactOther ->
            val group = on<StoreHandler>().store.box(Group::class).query().equal(Group_.id, groupContact.groupId!!).build().findFirst()
            val groupOther = on<StoreHandler>().store.box(Group::class).query().equal(Group_.id, groupContactOther.groupId!!).build().findFirst()

            if (group == null || groupOther == null) {
                return@Comparator 0
            }

            return@Comparator if (!on<DistanceHandler>().isPhoneNearGroup(group) && on<DistanceHandler>().isPhoneNearGroup(groupOther))
                1
            else if (!on<DistanceHandler>().isPhoneNearGroup(groupOther) && on<DistanceHandler>().isPhoneNearGroup(group))
                -1
            else if (group.updated == null || groupOther.updated == null)
                0
            else
                groupOther.updated!!.compareTo(group.updated)
        }
    }

    fun sortGroupActions(): Comparator<GroupAction> {
        return Comparator { o1, o2 ->
            if (o2.used == null && o1.used == null) o1.name!!.compareTo(o2.name!!)
            else (o2.used ?: Date(0)).compareTo(o1.used ?: Date(0))
        }
    }

    fun sortSuggestions(latLng: LatLng): Comparator<Suggestion> {
        return Comparator { o1, o2 ->
            val d1 = FloatArray(1)
            val d2 = FloatArray(1)

            distanceBetween(o1.latitude!!, o1.longitude!!, latLng.latitude, latLng.longitude, d1)
            distanceBetween(o2.latitude!!, o2.longitude!!, latLng.latitude, latLng.longitude, d2)

            return@Comparator if (d1[0] == d2[0]) 0 else if (d1[0] < d2[0]) -1 else 1
        }
    }

    fun sortPhysicalGroups(latLng: LatLng): Comparator<Group> {
        return Comparator { o1, o2 ->
            val d1 = FloatArray(1)
            val d2 = FloatArray(1)

            distanceBetween(o1.latitude!!, o1.longitude!!, latLng.latitude, latLng.longitude, d1)
            distanceBetween(o2.latitude!!, o2.longitude!!, latLng.latitude, latLng.longitude, d2)

            return@Comparator if (d1[0] == d2[0]) 0 else if (d1[0] < d2[0]) -1 else 1
        }
    }

    fun sortNotifications(): Comparator<Notification> {
        return Comparator { o1, o2 -> (o2.created ?: Date(0)).compareTo(o1.created ?: Date(0)) }
    }
}