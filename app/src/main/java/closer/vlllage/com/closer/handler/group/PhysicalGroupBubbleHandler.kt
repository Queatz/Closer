package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.Search
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.map.MapZoomHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*

class PhysicalGroupBubbleHandler constructor(private val on: On) {

    private val visiblePublicGroups = HashSet<String>()
    private var disposableGroup = on<DisposableHandler>().group()

    private fun update() {
            disposableGroup.clear()

            val latLng = on<MapHandler>().center ?: return

            disposableGroup.add(on<Search>().physicalGroups(latLng, privateOnly = on<AccountHandler>().privateOnly) { groups ->
                        val showGroups = groups.take(5)

                        for (group in showGroups) {
                            if (!visiblePublicGroups.contains(group.id)) {
                                val mapBubble = on<PhysicalGroupHandler>().physicalGroupBubbleFrom(group)

                                if (mapBubble != null) {
                                    on<BubbleHandler>().add(mapBubble)
                                }
                            }
                        }

                        visiblePublicGroups.clear()
                        for (group in showGroups) {
                            if (group.id == null) {
                                continue
                            }
                            visiblePublicGroups.add(group.id!!)
                        }

                        clearBubbles()
                    })
        }

    fun attach() {
        on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable().observeOn(AndroidSchedulers.mainThread()).subscribe {
            update()
        })

        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE).observeOn(AndroidSchedulers.mainThread()).subscribe {
            update()
        })

        on<DisposableHandler>().add(on<MapZoomHandler>().onZoomGreaterThanChanged(GEO_GROUPS_ZOOM).observeOn(AndroidSchedulers.mainThread()).subscribe(
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
