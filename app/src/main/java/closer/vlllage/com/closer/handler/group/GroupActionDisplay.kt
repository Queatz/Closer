package closer.vlllage.com.closer.handler.group

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.queatz.on.On
import kotlinx.android.synthetic.main.group_action_edit_flow_modal.view.*
import java.util.*

class GroupActionDisplay constructor(private val on: On) {
    fun display(view: View, groupAction: GroupAction, layout: Layout, scale: Float = 1f) {
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
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionSmallTextSize) * scale)
                holder.actionName.setBackgroundResource(R.drawable.gradient_shadow_top_rounded_8dp)
                holder.photo?.setImageDrawable(null)
                on<ImageHandler>().get().load(groupAction.photo!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=256")
                        .noPlaceholder()
                        .into(holder.photo)
            } else {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionLargeTextSize) * scale)
                holder.actionName.background = null
                holder.photo?.setImageResource(getRandomBubbleBackgroundResource(groupAction))
            }
        }
    }

    fun onGroupActionClick(groupAction: GroupAction, view: View?) {
        startGroupActionFlow(groupAction, view, groupAction.flow?.let { on<JsonHandler>().from(it, JsonArray::class.java) } ?: JsonArray(0))
    }

    private fun startGroupActionFlow(groupAction: GroupAction, view: View?, flowRemaining: JsonArray, accumulator: String? = null) {
        if (flowRemaining.size() > 0) {
            val skippable = flowRemaining.first().asJsonObject.has("skippable") && flowRemaining.first().asJsonObject["skippable"].asBoolean

            on<MenuHandler>().show(
                    *flowRemaining.first().asJsonObject["options"].asJsonArray.map { option ->
                        MenuHandler.MenuOption(0, title = option.asString) {
                            flowRemaining.remove(0)
                            startGroupActionFlow(groupAction, view, flowRemaining, accumulator?.let { "$accumulator\n☑ ${option.asString}" } ?: "☑ ${option.asString}")
                        }
                    }.toTypedArray(),
                    title = groupAction.name,
                    button = if (skippable) on<ResourcesHandler>().resources.getString(R.string.skip) else "",
                    buttonCallback = {
                        flowRemaining.remove(0)
                        startGroupActionFlow(groupAction, view, flowRemaining, accumulator)
                    })
        } else {
            onGroupActionSelection(groupAction, view, accumulator)
        }
    }

    fun onGroupActionSelection(groupAction: GroupAction, view: View?, selection: String?) {
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
                val success = on<GroupMessageAttachmentHandler>().groupActionReply(groupAction.group!!, groupAction, "${selection?.let { if (comment.isBlank()) it else "$it\n\n" } ?: ""}${comment}")
                if (!success) {
                    on<DefaultAlerts>().thatDidntWork()
                } else {
                    on<GroupActivityTransitionHandler>().showGroupMessages(view, groupAction.group)
                }
            }
            title = on<AccountHandler>().name + " " + groupAction.intent
            message = "${group.name?.let { on<ResourcesHandler>().resources.getString(R.string.in_x, it) } ?: ""}${selection?.let { if (group.name.isNullOrBlank()) it else "\n\n$it"} ?: ""}"
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
                    MenuHandler.MenuOption(R.drawable.ic_poll_black_24dp, R.string.edit_flow) { editGroupActionFlow(groupAction) },
                    MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_action_menu_item) { removeGroupAction(groupAction) }
            )
        }
    }

    private fun editGroupActionFlow(groupAction: GroupAction) {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.save)
            positiveButtonCallback = { result ->
                val view = result as ViewGroup

                val flow = JsonArray()

                if (view.useOptions.isChecked) {
                    val options = listOf(view.option1, view.option2, view.option3).map { it.text.toString() }.filter { it.isNotBlank() }

                    if (options.isNotEmpty()) {
                        flow.add(JsonObject().apply {
                            add("options", JsonArray().apply { options.forEach { add(JsonPrimitive(it)) } })
                            add("skippable", JsonPrimitive(view.skippable.isChecked))
                        })
                    }
                }

                on<GroupActionUpgradeHandler>().setFlow(groupAction, flow)
            }
            layoutResId = R.layout.group_action_edit_flow_modal
            onAfterViewCreated = { alertConfig, view ->
                alertConfig.alertResult = view

                view.useOptions.setOnCheckedChangeListener { _, isChecked ->
                    view.option1.visible = isChecked
                    view.option2.visible = isChecked
                    view.option3.visible = isChecked
                    view.skippable.visible = isChecked
                }

                val options = listOf(view.option1, view.option2, view.option3)
                val flowArray = groupAction.flow?.let { on<JsonHandler>().from(it, JsonArray::class.java) }

                flowArray?.firstOrNull()?.let { step ->
                    step.asJsonObject["options"].asJsonArray.take(options.size).forEachIndexed { index, stepOption ->
                        options[index].setText(stepOption.asString)
                    }
                    view.skippable.isChecked = step.asJsonObject["skippable"]?.asBoolean ?: false
                }

                view.useOptions.isChecked = flowArray?.size() ?: 0 > 0
            }
            title = on<ResourcesHandler>().resources.getString(R.string.edit_flow)
            show()
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