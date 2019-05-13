package closer.vlllage.com.closer.handler.group

import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.HOUR_IN_MILLIS
import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapZoomHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import java.util.*

class PhysicalGroupBubbleHandler : PoolMember() {

    private val visiblePublicGroups = HashSet<String>()
    private var physicalGroupSubscription: DataSubscription? = null

    private val newPhysicalGroupObservable: DataSubscription?
        get() {
            if (physicalGroupSubscription != null) {
                `$`(DisposableHandler::class.java).dispose(physicalGroupSubscription!!)
            }

            val oneHourAgo = Date()
            oneHourAgo.time = oneHourAgo.time - HOUR_IN_MILLIS

            val oneMonthAgo = Date()
            oneMonthAgo.time = oneMonthAgo.time - 90 * DAY_IN_MILLIS

            physicalGroupSubscription = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                    .equal(Group_.physical, true)
                    .and()
                    .greater(Group_.updated, oneHourAgo)
                    .or()
                    .equal(Group_.hub, true)
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { groups ->
                        clearBubbles()
                        for (group in groups) {
                            if (!visiblePublicGroups.contains(group.id)) {
                                val mapBubble = `$`(PhysicalGroupHandler::class.java).physicalGroupBubbleFrom(group)

                                if (mapBubble != null) {
                                    `$`(BubbleHandler::class.java).add(mapBubble)
                                }
                            }
                        }

                        visiblePublicGroups.clear()
                        for (group in groups) {
                            if (group.id == null) {
                                continue
                            }
                            visiblePublicGroups.add(group.id!!)
                        }
                    }

            `$`(DisposableHandler::class.java).add(physicalGroupSubscription!!)

            return physicalGroupSubscription
        }

    fun attach() {
        `$`(DisposableHandler::class.java).add(`$`(MapZoomHandler::class.java).onZoomGreaterThanChanged(GEO_GROUPS_ZOOM).subscribe(
                { zoomIsGreaterThan15 ->
                    if (zoomIsGreaterThan15!!) {
                        `$`(DisposableHandler::class.java).add(newPhysicalGroupObservable!!)
                    } else {
                        visiblePublicGroups.clear()
                        clearBubbles()
                        if (physicalGroupSubscription != null) {
                            `$`(DisposableHandler::class.java).dispose(physicalGroupSubscription!!)
                            physicalGroupSubscription = null
                        }
                    }

                }, { it.printStackTrace() }
        ))
    }

    private fun clearBubbles() {
        `$`(BubbleHandler::class.java).remove { mapBubble -> mapBubble.type == BubbleType.PHYSICAL_GROUP && !visiblePublicGroups.contains((mapBubble.tag as Group).id) }
    }

    companion object {
        private const val GEO_GROUPS_ZOOM = 14f
    }
}
