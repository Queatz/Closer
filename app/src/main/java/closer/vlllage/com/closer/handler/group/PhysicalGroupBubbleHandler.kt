package closer.vlllage.com.closer.handler.group

import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.HOUR_IN_MILLIS
import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.MapZoomHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import java.util.*

class PhysicalGroupBubbleHandler constructor(private val on: On) {

    private val visiblePublicGroups = HashSet<String>()
    private var physicalGroupSubscription: DataSubscription? = null

    private val newPhysicalGroupObservable: DataSubscription?
        get() {
            if (physicalGroupSubscription != null) {
                on<DisposableHandler>().dispose(physicalGroupSubscription!!)
            }

            val oneHourAgo = Date()
            oneHourAgo.time = oneHourAgo.time - HOUR_IN_MILLIS

            val oneMonthAgo = Date()
            oneMonthAgo.time = oneMonthAgo.time - 90 * DAY_IN_MILLIS

            physicalGroupSubscription = on<StoreHandler>().store.box(Group::class).query()
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
                                val mapBubble = on<PhysicalGroupHandler>().physicalGroupBubbleFrom(group)

                                if (mapBubble != null) {
                                    on<BubbleHandler>().add(mapBubble)
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

            on<DisposableHandler>().add(physicalGroupSubscription!!)

            return physicalGroupSubscription
        }

    fun attach() {
        on<DisposableHandler>().add(on<MapZoomHandler>().onZoomGreaterThanChanged(GEO_GROUPS_ZOOM).subscribe(
                { zoomIsGreaterThan12 ->
                    if (zoomIsGreaterThan12!!) {
                        on<DisposableHandler>().add(newPhysicalGroupObservable!!)
                    } else {
                        visiblePublicGroups.clear()
                        clearBubbles()
                        if (physicalGroupSubscription != null) {
                            on<DisposableHandler>().dispose(physicalGroupSubscription!!)
                            physicalGroupSubscription = null
                        }
                    }

                }, { it.printStackTrace() }
        ))
    }

    private fun clearBubbles() {
        on<BubbleHandler>().remove { mapBubble -> mapBubble.type == BubbleType.PHYSICAL_GROUP && !visiblePublicGroups.contains((mapBubble.tag as Group).id) }
    }

    companion object {
        private const val GEO_GROUPS_ZOOM = 12f
    }
}
