package closer.vlllage.com.closer.handler.group

import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import java.util.*

class GroupActionDisplay constructor(private val on: On) {
    fun display(view: View, groupAction: GroupAction, layout: Layout) {
        view.clipToOutline = true

        val holder = GroupActionViewHolder(view)
        holder.actionName.text = groupAction.name

        val target: View = when (layout) {
            Layout.PHOTO -> holder.itemView
            Layout.TEXT -> holder.actionName
        }

        target.setOnClickListener {
            onGroupActionClick(groupAction, view)
        }

        target.setOnLongClickListener {
            onGroupActionLongClick(groupAction)
            return@setOnLongClickListener true
        }

        if (layout == Layout.PHOTO) {
            val group = on<StoreHandler>().store.box(Group::class).query()
                    .equal(Group_.id, groupAction.group!!)
                    .build()
                    .findFirst()

            holder.groupName?.text = group?.name ?: ""

            when (getRandom(groupAction).nextInt(4)) {
                1 -> holder.itemView.setBackgroundResource(R.drawable.clickable_blue_8dp)
                2 -> holder.itemView.setBackgroundResource(R.drawable.clickable_accent_8dp)
                3 -> holder.itemView.setBackgroundResource(R.drawable.clickable_green_8dp)
                else -> holder.itemView.setBackgroundResource(R.drawable.clickable_red_8dp)
            }

            if (groupAction.photo != null) {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionSmallTextSize))
                holder.actionName.setBackgroundResource(R.drawable.gradient_shadow_top_rounded_8dp)
                holder.photo?.setImageDrawable(null)
                on<ImageHandler>().get().load(groupAction.photo!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=256")
                        .noPlaceholder()
                        .into(holder.photo)
            } else {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionLargeTextSize))
                holder.actionName.background = null
                holder.photo?.setImageResource(getRandomBubbleBackgroundResource(groupAction))
            }
        }
    }

    fun onGroupActionClick(groupAction: GroupAction, view: View?) {
        val group = on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupAction.group!!).build().findFirst()

        if (group == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        on<AlertHandler>().make().apply {
            layoutResId = R.layout.comments_modal
            textViewId = R.id.input
            onTextViewSubmitCallback = { comment ->
                val success = on<GroupMessageAttachmentHandler>().groupActionReply(groupAction.group!!, groupAction, comment)
                if (!success) {
                    on<DefaultAlerts>().thatDidntWork()
                } else {
                    on<GroupActivityTransitionHandler>().showGroupMessages(view, groupAction.group)
                }
            }
            title = on<AccountHandler>().name + " " + groupAction.intent
            message = group.name ?: ""
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.post)
            show()
        }
    }

    fun onGroupActionLongClick(groupAction: GroupAction) {
        if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_open_in_new_black_24dp, R.string.open_group) { on<GroupActivityTransitionHandler>().showGroupMessages(null, groupAction.group) },
                    MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, R.string.share_group_activity) { shareGroupActivity(groupAction) },
                    MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo) { takeGroupActionPhoto(groupAction) },
                    MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo) { uploadGroupActionPhoto(groupAction) },
                    MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_action_menu_item) { removeGroupAction(groupAction) }
            )
        }
    }

    private fun shareGroupActivity(groupAction: GroupAction) {
        on<ShareActivityTransitionHandler>().shareGroupActionToGroup(groupAction.id!!)
    }

    private fun uploadGroupActionPhoto(groupAction: GroupAction) {
        on<GroupActionUpgradeHandler>().setPhotoFromMedia(groupAction)
    }

    private fun takeGroupActionPhoto(groupAction: GroupAction) {
        on<GroupActionUpgradeHandler>().setPhotoFromCamera(groupAction)
    }

    private fun removeGroupAction(groupAction: GroupAction) {
        on<AlertHandler>().make().apply {
            message = on<ResourcesHandler>().resources.getString(R.string.remove_action_message, groupAction.name)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.remove_action)
            positiveButtonCallback = {
                on<DisposableHandler>().add(on<ApiHandler>().removeGroupAction(groupAction.id!!).subscribe(
                        { on<StoreHandler>().store.box(GroupAction::class).remove(groupAction) },
                        { on<DefaultAlerts>().thatDidntWork() }
                ))
            }
            show()
        }
    }

    @DrawableRes
    private fun getRandomBubbleBackgroundResource(groupAction: GroupAction) = when (getRandom(groupAction).nextInt(3)) {
        0 -> R.drawable.bkg_bubbles
        1 -> R.drawable.bkg_bubbles_2
        else -> R.drawable.bkg_bubbles_3
    }

    private fun getRandom(groupAction: GroupAction): Random {
        return Random(if (groupAction.id == null)
            groupAction.objectBoxId
        else
            groupAction.id!!.hashCode().toLong())
    }

    inner class GroupActionViewHolder(val itemView: View) {

        var photo: ImageView? = itemView.findViewById(R.id.photo)
        var actionName: TextView = itemView.findViewById(R.id.actionName)
        var groupName: TextView? = itemView.findViewById(R.id.groupName)

        init {
            itemView.clipToOutline = true
        }
    }

    enum class Layout {
        TEXT,
        PHOTO
    }
}