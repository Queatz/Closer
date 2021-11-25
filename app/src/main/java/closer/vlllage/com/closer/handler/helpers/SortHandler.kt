package closer.vlllage.com.closer.handler.helpers

import android.location.Location.distanceBetween
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On
import io.objectbox.query.QueryBuilder
import java.util.*
import kotlin.math.abs

class SortHandler constructor(private val on: On) {

    fun sortGroupsByDistance(latLng: LatLng): Comparator<Group> = Comparator { o1, o2 ->
        if (o1.latitude == null || o1.longitude == null) {
            if (o2.latitude == null || o2.longitude == null) {
                return@Comparator 0
            } else return@Comparator 1
        } else if (o2.latitude == null || o2.longitude == null) {
            return@Comparator -1
        }

        val d1 = FloatArray(1)
        val d2 = FloatArray(1)

        distanceBetween(o1.latitude!!, o1.longitude!!, latLng.latitude, latLng.longitude, d1)
        distanceBetween(o2.latitude!!, o2.longitude!!, latLng.latitude, latLng.longitude, d2)

        return@Comparator if (d1[0] == d2[0]) 0 else if (d1[0] < d2[0]) -1 else 1
    }

    fun sortGroups(privateFirst: Boolean = true): Comparator<Group> {
        return Comparator { group, groupOther ->
            val nearGroup = on<DistanceHandler>().isPhoneNearGroup(group)
            val nearGroupOther = on<DistanceHandler>().isPhoneNearGroup(groupOther)

            if (!nearGroup && nearGroupOther)
                1
            else if (!nearGroupOther && nearGroup)
                -1
            else if (privateFirst && group.isPublic && !groupOther.isPublic)
                1
            else if (privateFirst && groupOther.isPublic && !group.isPublic)
                -1
            else
                (groupOther.updated ?: Date(0)).compareTo(group.updated ?: Date(0))
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
            val group = on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupContact.groupId!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().findFirst()
            val groupOther = on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupContactOther.groupId!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build().findFirst()

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

    fun sortEvents(latLng: LatLng): Comparator<Event> {
        return Comparator { o1, o2 ->

            if (o1.startsAt != o2.startsAt) {
                return@Comparator if (o1.startsAt!!.before(o2.startsAt)) -1 else 1
            }

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

    fun sortQuests(latLng: LatLng): Comparator<Quest> {
        return Comparator { o1, o2 ->
            val questProgresses = on<StoreHandler>().store.box(QuestProgress::class).query()
                    .equal(QuestProgress_.questId, o1.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .equal(QuestProgress_.active, true)
                    .equal(QuestProgress_.ofId, on<PersistenceHandler>().phoneId!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .build()
                    .count()

            val questProgressesOther = on<StoreHandler>().store.box(QuestProgress::class).query()
                    .equal(QuestProgress_.questId, o2.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .equal(QuestProgress_.active, true)
                    .equal(QuestProgress_.ofId, on<PersistenceHandler>().phoneId!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                    .build()
                    .count()

            if (questProgresses > questProgressesOther) {
                return@Comparator -1
            } else if (questProgresses < questProgressesOther) {
                return@Comparator 1
            }

            val d1 = FloatArray(1)
            val d2 = FloatArray(1)

            distanceBetween(o1.latitude!!, o1.longitude!!, latLng.latitude, latLng.longitude, d1)
            distanceBetween(o2.latitude!!, o2.longitude!!, latLng.latitude, latLng.longitude, d2)

            if (abs(d1[0] - d2[0]) < on<HowFar>().about1Mile) {
                return@Comparator (o2.updated ?: Date(0)).compareTo(o1.updated ?: Date(0))
            }

            return@Comparator if (d1[0] == d2[0]) 0 else if (d1[0] < d2[0]) -1 else 1
        }
    }

    fun sortQuestProgresses(): Comparator<QuestProgress> {
        return Comparator { o1, o2 ->
            val me = on<PersistenceHandler>().phoneId

            if (o1.active == true && o2.active == false) {
                return@Comparator -1
            } else if (o1.active == false && o2.active == true) {
                return@Comparator 1
            }

            if (o1.ofId == me && o2.ofId != me) {
                return@Comparator -1
            } else if (o1.ofId != me && o2.ofId == me) {
                return@Comparator 1
            }

            return@Comparator 0
        }
    }

    fun sortLifestyles(): Comparator<Lifestyle> {
        return Comparator { o1, o2 ->
            (o2.phonesCount ?: 0).compareTo(o1.phonesCount ?: 0)
        }
    }

    fun sortGoals(): Comparator<Goal> {
        return Comparator { o1, o2 ->
            (o2.phonesCount ?: 0).compareTo(o1.phonesCount ?: 0)
        }
    }
}