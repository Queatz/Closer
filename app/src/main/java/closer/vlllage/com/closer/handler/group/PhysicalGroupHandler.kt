package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.OnSyncResult
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class PhysicalGroupHandler constructor(private val on: On) {

    fun createPhysicalGroup(latLng: LatLng, isPublic: Boolean = true, name: String = "", onSyncResult: OnSyncResult? = null) {
        val group = on<StoreHandler>().create(Group::class.java)
        group!!.name = name
        group.about = ""
        group.isPublic = isPublic
        group.physical = true
        group.latitude = latLng.latitude
        group.longitude = latLng.longitude
        on<StoreHandler>().store.box(Group::class).put(group)
        on<SyncHandler>().sync(group, onSyncResult ?: { id ->
            openGroup(id)
        })
    }

    private fun openGroup(groupId: String) {
        on<GroupActivityTransitionHandler>().showGroupMessages(null, groupId, true)
    }

    fun physicalGroupBubbleFrom(group: Group): MapBubble? {
        if (group.latitude == null || group.longitude == null) {
            return null
        }
        val mapBubble = MapBubble(LatLng(group.latitude!!, group.longitude!!), "Group", "")
        mapBubble.type = BubbleType.PHYSICAL_GROUP
        mapBubble.isPinned = true
        mapBubble.tag = group
        return mapBubble
    }
}
