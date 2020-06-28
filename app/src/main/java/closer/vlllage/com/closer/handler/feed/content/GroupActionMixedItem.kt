package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On
import kotlinx.android.synthetic.main.group_action_photo_large_item.view.*

class GroupActionMixedItem(val groupAction: GroupAction) : MixedItem(MixedItemType.GroupAction)

class GroupActionViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.GroupAction) {
    lateinit var on: On
    lateinit var disposableGroup: DisposableGroup
}

class GroupActionMixedItemAdapter(private val on: On) : MixedItemAdapter<GroupActionMixedItem, GroupActionViewHolder> {
    override fun bind(holder: GroupActionViewHolder, item: GroupActionMixedItem, position: Int) {
        bindGroupAction(holder, item.groupAction)
    }

    override fun getMixedItemClass() = GroupActionMixedItem::class
    override fun getMixedItemType() = MixedItemType.GroupAction

    override fun areItemsTheSame(old: GroupActionMixedItem, new: GroupActionMixedItem) = old.groupAction.objectBoxId == new.groupAction.objectBoxId

    override fun areContentsTheSame(old: GroupActionMixedItem, new: GroupActionMixedItem) = old.groupAction.about == new.groupAction.about

    override fun onCreateViewHolder(parent: ViewGroup) = GroupActionViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.group_action_photo_large_item, parent, false))

    override fun onViewRecycled(holder: GroupActionViewHolder) {
        holder.on.off()
    }

    private fun bindGroupAction(holder: GroupActionViewHolder, groupAction: GroupAction) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<GroupActionDisplay>()
        }
        holder.on<GroupActionDisplay>().display(holder.itemView.groupAction, groupAction, GroupActionDisplay.Layout.PHOTO, holder.itemView.groupActionDescription, 1.5f)
    }
}