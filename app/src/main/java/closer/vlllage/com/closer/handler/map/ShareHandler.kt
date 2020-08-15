package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.*
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler

class ShareHandler constructor(private val on: On) {

    fun shareTo(latLng: LatLng, onGroupSelectedListener: (Group) -> Unit) {
        on<StoreHandler>().store.box(Group::class).query()
                .notEqual(Group_.physical, true)
                .sort(on<SortHandler>().sortGroups(false))
                .build().subscribe().single().on(AndroidScheduler.mainThread()).observer { groups ->
                    val groupNames = mutableListOf<MapBubbleMenuItem>()
                    for (group in groups) {
                        group.name ?: continue
                        groupNames.add(MapBubbleMenuItem(group.name!!, if (group.isPublic) R.drawable.ic_public_black_18dp else R.drawable.ic_group_black_18dp))
                    }

                    val menuBubble = MapBubble(latLng, BubbleType.MENU, true, true)
                    menuBubble.isPinned = true
                    menuBubble.isOnTop = true
                    on<TimerHandler>().postDisposable({
                        on<BubbleHandler>().add(menuBubble)
                        menuBubble.onViewReadyListener = {
                            on<MapBubbleMenuView>().setMenuTitle(menuBubble, on<ResourcesHandler>().resources.getString(R.string.share))
                            on<MapBubbleMenuView>().getMenuAdapter(menuBubble).setMenuItems(groupNames)
                            menuBubble.onItemClickListener = { position ->
                                    onGroupSelectedListener.invoke(groups[position])
                            }
                        }
                    }, 225)
                }
    }
}
