package closer.vlllage.com.closer.handler.group

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.Group_

class GroupActionRecyclerViewHandler constructor(private val on: On) {

    var adapter: GroupActionAdapter? = null
        private set
    var recyclerView: RecyclerView? = null
        private set
    var onGroupActionRepliedListener: OnGroupActionRepliedListener? = null

    fun attach(actionRecyclerView: RecyclerView, layout: GroupActionAdapter.Layout) {
        this.recyclerView = actionRecyclerView
        actionRecyclerView.layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        adapter = GroupActionAdapter(on, layout, { groupAction ->
            val group = on<StoreHandler>().store.box(Group::class).query()
                    .equal(Group_.id, groupAction.group!!).build().findFirst()

            if (group == null) {
                on<DefaultAlerts>().thatDidntWork()
                return@GroupActionAdapter
            }

            on<AlertHandler>().make().apply {
                layoutResId = R.layout.comments_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = { comment ->
                    val success = on<GroupMessageAttachmentHandler>().groupActionReply(groupAction.group!!, groupAction, comment)
                    if (!success) {
                        on<DefaultAlerts>().thatDidntWork()
                    } else {
                        onGroupActionRepliedListener?.invoke(groupAction)
                    }
                }
                title = groupAction.name
                message = group.name
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.post)
                show()
            }
        }, { groupAction ->
            if (on<FeatureHandler>().has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
                on<MenuHandler>().show(
                        MenuHandler.MenuOption(R.drawable.ic_open_in_new_black_24dp, R.string.open_group) { on<GroupActivityTransitionHandler>().showGroupMessages(null, groupAction.group) },
                        MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo) { takeGroupActionPhoto(groupAction) },
                        MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo) { uploadGroupActionPhoto(groupAction) },
                        MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_action_menu_item) { removeGroupAction(groupAction) }
                )
            }
        })

        actionRecyclerView.adapter = adapter
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
}

typealias OnGroupActionRepliedListener = (groupAction: GroupAction) -> Unit
