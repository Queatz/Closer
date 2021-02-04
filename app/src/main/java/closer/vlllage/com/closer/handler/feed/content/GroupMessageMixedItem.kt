package closer.vlllage.com.closer.handler.feed.content

import android.view.ViewGroup
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageHelper
import closer.vlllage.com.closer.handler.group.GroupMessageViewHolder
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import com.queatz.on.On

class GroupMessageMixedItem(val groupMessage: GroupMessage) : MixedItem(MixedItemType.GroupMessage)

class GroupMessageMixedItemAdapter(private val on: On) : MixedItemAdapter<GroupMessageMixedItem, GroupMessageViewHolder> {
    override fun bind(holder: GroupMessageViewHolder, item: GroupMessageMixedItem, position: Int) {
        bindGroupMessage(holder, item.groupMessage)
    }

    override fun getMixedItemClass() = GroupMessageMixedItem::class
    override fun getMixedItemType() = MixedItemType.GroupMessage

    override fun areItemsTheSame(old: GroupMessageMixedItem, new: GroupMessageMixedItem) =
            on<GroupMessageHelper>().areItemsTheSame(old.groupMessage, new.groupMessage)

    override fun areContentsTheSame(old: GroupMessageMixedItem, new: GroupMessageMixedItem) =
            on<GroupMessageHelper>().areContentsTheSame(old.groupMessage, new.groupMessage)

    override fun onCreateViewHolder(parent: ViewGroup) = on<GroupMessageHelper>().createViewHolder(parent)

    override fun onViewRecycled(holder: GroupMessageViewHolder) {
        on<GroupMessageHelper>().recycleViewHolder(holder)
        holder.on.off()
    }

    private fun bindGroupMessage(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<LightDarkHandler>().setLight(true)
            use<GroupMessageHelper> {
                global = true
                inFeed = true
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
                onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group1.id) }
            }
        }
        holder.on<GroupMessageHelper>().onBind(groupMessage, null, holder)
    }
}