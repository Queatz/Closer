package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.GroupActionPhotoLargeItemBinding
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On

class GroupActionMixedItem(val groupAction: GroupAction) : MixedItem(MixedItemType.GroupAction)

class GroupActionViewHolder(val binding: GroupActionPhotoLargeItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.GroupAction) {
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

    override fun onCreateViewHolder(parent: ViewGroup) = GroupActionViewHolder(GroupActionPhotoLargeItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: GroupActionViewHolder) {
        holder.on.off()
    }

    private fun bindGroupAction(holder: GroupActionViewHolder, groupAction: GroupAction) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<GroupActionDisplay>()
        }

        holder.on<GroupActionDisplay>().onGroupActionClickListener = { groupAction, proceed ->
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.share_this)) {
                        on<ShareActivityTransitionHandler>().shareGroupActionToGroup(groupAction.id!!)
                    },
                    MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.post_now)) {
                        holder.on<GroupActionDisplay>().fallbackGroupActionClickListener.invoke(groupAction, proceed)
                    },
                    button = ""
            )
        }

        holder.on<GroupActionDisplay>().display(holder.binding.groupAction, groupAction, GroupActionDisplay.Layout.PHOTO, holder.binding.groupActionDescription, 1.5f)
    }
}