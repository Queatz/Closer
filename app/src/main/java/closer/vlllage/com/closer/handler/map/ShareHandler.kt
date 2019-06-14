package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.*
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.LatLng
import io.objectbox.android.AndroidScheduler
import java.util.*

class ShareHandler constructor(private val on: On) {

    fun shareTo(latLng: LatLng, onGroupSelectedListener: (Group) -> Unit) {
        on<StoreHandler>().store.box(Group::class).query()
                .notEqual(Group_.physical, true)
                .sort(on<SortHandler>().sortGroups(false))
                .build().subscribe().single().on(AndroidScheduler.mainThread()).observer { groups ->
                    val groupNames = ArrayList<MapBubbleMenuItem>()
                    for (group in groups) {
                        groupNames.add(MapBubbleMenuItem(group.name!!))
                    }

                    val menuBubble = MapBubble(latLng, BubbleType.MENU)
                    menuBubble.isPinned = true
                    menuBubble.isOnTop = true
                    on<TimerHandler>().postDisposable(Runnable {
                        on<BubbleHandler>().add(menuBubble)
                        menuBubble.onViewReadyListener = {
                            on<MapBubbleMenuView>().setMenuTitle(menuBubble, on<ResourcesHandler>().resources.getString(R.string.share_with))
                            on<MapBubbleMenuView>().getMenuAdapter(menuBubble).setMenuItems(groupNames)
                            menuBubble.onItemClickListener = { position ->
                                    onGroupSelectedListener.invoke(groups[position])
                            }
                        }
                    }, 225)
                }
    }
}
