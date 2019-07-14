package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.map.MapZoomHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import java.util.*

class PhysicalGroupBubbleHandler constructor(private val on: On) {

    private val visiblePublicGroups = HashSet<String>()
    private var disposableGroup = on<DisposableHandler>().group()

    private fun update() {
            disposableGroup.clear()

            val distance = 0.12f

            val latLng = on<MapHandler>().center!!

            disposableGroup.add(on<StoreHandler>().store.box(Group::class).query()
                    .equal(Group_.physical, true)
                    .between(Group_.latitude, latLng.latitude - distance, latLng.latitude + distance)
                    .between(Group_.longitude, latLng.longitude - distance, latLng.longitude + distance)
                    .sort(on<SortHandler>().sortPhysicalGroups(latLng))
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { groups ->
                        for (group in groups.take(5)) {
                            if (!visiblePublicGroups.contains(group.id)) {
                                val mapBubble = on<PhysicalGroupHandler>().physicalGroupBubbleFrom(group)

                                if (mapBubble != null) {
                                    on<BubbleHandler>().add(mapBubble)
                                }
                            }
                        }

                        visiblePublicGroups.clear()
                        for (group in groups.take(5)) {
                            if (group.id == null) {
                                continue
                            }
                            visiblePublicGroups.add(group.id!!)
                        }

                        clearBubbles()
                    })
        }

    fun attach() {
        on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable().subscribe {
            update()
        })

        on<DisposableHandler>().add(on<MapZoomHandler>().onZoomGreaterThanChanged(GEO_GROUPS_ZOOM).subscribe(
                { zoomIsGreaterThan14 ->
                    if (zoomIsGreaterThan14) {
                        update()
                    } else {
                        disposableGroup.clear()
                        visiblePublicGroups.clear()
                        clearBubbles()
                    }

                }, { it.printStackTrace() }
        ))
    }

    private fun clearBubbles() {
        on<BubbleHandler>().remove { mapBubble -> mapBubble.type == BubbleType.PHYSICAL_GROUP && !visiblePublicGroups.contains((mapBubble.tag as Group).id) }
    }

    companion object {
        private const val GEO_GROUPS_ZOOM = 14f
    }
}
