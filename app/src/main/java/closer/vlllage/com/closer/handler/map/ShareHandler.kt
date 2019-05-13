package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.*
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import com.google.android.gms.maps.model.LatLng
import io.objectbox.android.AndroidScheduler
import java.util.*

class ShareHandler : PoolMember() {

    fun shareTo(latLng: LatLng, onGroupSelectedListener: (Group) -> Unit) {
        `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                .notEqual(Group_.physical, true)
                .sort(`$`(SortHandler::class.java).sortGroups())
                .build().subscribe().single().on(AndroidScheduler.mainThread()).observer { groups ->
                    val groupNames = ArrayList<MapBubbleMenuItem>()
                    for (group in groups) {
                        groupNames.add(MapBubbleMenuItem(group.name!!))
                    }

                    val menuBubble = MapBubble(latLng, BubbleType.MENU)
                    menuBubble.isPinned = true
                    menuBubble.isOnTop = true
                    `$`(TimerHandler::class.java).postDisposable(Runnable {
                        `$`(BubbleHandler::class.java).add(menuBubble)
                        menuBubble.onViewReadyListener = {
                            `$`(MapBubbleMenuView::class.java).setMenuTitle(menuBubble, `$`(ResourcesHandler::class.java).resources.getString(R.string.share_with))
                            `$`(MapBubbleMenuView::class.java).getMenuAdapter(menuBubble).setMenuItems(groupNames)
                            menuBubble.onItemClickListener = { position ->
                                    onGroupSelectedListener.invoke(groups[position])
                            }
                        }
                    }, 225)
                }
    }
}
