package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import java.util.*

class SortHandler constructor(private val on: On) {

    @JvmOverloads
    fun sortGroups(privateFirst: Boolean = true): Comparator<Group> {
        return Comparator { group, groupOther ->
            if (!on<DistanceHandler>().isUserNearGroup(group) && on<DistanceHandler>().isUserNearGroup(groupOther))
                1
            else if (!on<DistanceHandler>().isUserNearGroup(groupOther) && on<DistanceHandler>().isUserNearGroup(group))
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

    fun sortGroupMessages(): Comparator<GroupMessage> {
        return Comparator { groupMessage, groupMessageOther -> if (groupMessage.time == null || groupMessageOther.time == null) 0 else groupMessageOther.time!!.compareTo(groupMessage.time) }
    }

    fun sortPhones(): Comparator<Phone> {
        return Comparator { phone, phoneOther -> if (phone.updated == null || phoneOther.updated == null) 0 else phoneOther.updated!!.compareTo(phone.updated) }
    }

    fun sortGroupContacts(): Comparator<GroupContact> {
        return Comparator { groupContact, groupContactOther ->
            val group = on<StoreHandler>().store.box(Group::class).query().equal(Group_.id, groupContact.groupId!!).build().findFirst()
            val groupOther = on<StoreHandler>().store.box(Group::class).query().equal(Group_.id, groupContactOther.groupId!!).build().findFirst()

            if (group == null || groupOther == null) {
                return@Comparator 0
            }

            return@Comparator if (!on<DistanceHandler>().isUserNearGroup(group) && on<DistanceHandler>().isUserNearGroup(groupOther))
                1
            else if (!on<DistanceHandler>().isUserNearGroup(groupOther) && on<DistanceHandler>().isUserNearGroup(group))
                -1
            else if (group.updated == null || groupOther.updated == null)
                0
            else
                groupOther.updated!!.compareTo(group.updated)
        }
    }
}
