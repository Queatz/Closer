package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import com.google.android.gms.maps.model.LatLng

class PhysicalGroupHandler : PoolMember() {

    fun createPhysicalGroup(latLng: LatLng) {
        val group = `$`(StoreHandler::class.java).create(Group::class.java)
        group!!.name = ""
        group.about = ""
        group.isPublic = true
        group.physical = true
        group.latitude = latLng.latitude
        group.longitude = latLng.longitude
        `$`(StoreHandler::class.java).store.box(Group::class.java).put(group)
        `$`(SyncHandler::class.java).sync(group, { id ->
            openGroup(id)
        })
    }

    private fun openGroup(groupId: String) {
        `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, groupId, true)
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
