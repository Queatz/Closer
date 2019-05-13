package closer.vlllage.com.closer.handler.group

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.FeatureHandler
import closer.vlllage.com.closer.handler.FeatureType
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.Group_

class GroupActionRecyclerViewHandler : PoolMember() {

    var adapter: GroupActionAdapter? = null
        private set
    var recyclerView: RecyclerView? = null
        private set
    var onGroupActionRepliedListener: OnGroupActionRepliedListener? = null

    fun attach(actionRecyclerView: RecyclerView, layout: GroupActionAdapter.Layout) {
        this.recyclerView = actionRecyclerView
        actionRecyclerView.layoutManager = LinearLayoutManager(
                `$`(ActivityHandler::class.java).activity,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        adapter = GroupActionAdapter(this, layout, { groupAction ->
            val group = `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                    .equal(Group_.id, groupAction.group!!).build().findFirst()

            if (group == null) {
                `$`(DefaultAlerts::class.java).thatDidntWork()
                return@GroupActionAdapter
            }

            `$`(AlertHandler::class.java).make().apply {
                layoutResId = R.layout.comments_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = { comment ->
                    val success = `$`(GroupMessageAttachmentHandler::class.java).groupActionReply(groupAction.group!!, groupAction, comment)
                    if (!success) {
                        `$`(DefaultAlerts::class.java).thatDidntWork()
                    } else {
                        onGroupActionRepliedListener?.invoke(groupAction)
                    }
                }
                title = groupAction.name
                message = group.name
                positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.post)
                show()
            }
        }, { groupAction ->
            if (`$`(FeatureHandler::class.java).has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
                `$`(MenuHandler::class.java).show(
                        MenuHandler.MenuOption(R.drawable.ic_open_in_new_black_24dp, R.string.open_group) { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, groupAction.group) },
                        MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo) { takeGroupActionPhoto(groupAction) },
                        MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo) { uploadGroupActionPhoto(groupAction) },
                        MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_action_menu_item) { removeGroupAction(groupAction) }
                )
            }
        })

        actionRecyclerView.adapter = adapter
    }

    private fun uploadGroupActionPhoto(groupAction: GroupAction) {
        `$`(GroupActionUpgradeHandler::class.java).setPhotoFromMedia(groupAction)
    }

    private fun takeGroupActionPhoto(groupAction: GroupAction) {
        `$`(GroupActionUpgradeHandler::class.java).setPhotoFromCamera(groupAction)
    }

    private fun removeGroupAction(groupAction: GroupAction) {
        `$`(AlertHandler::class.java).make().apply {
            message = `$`(ResourcesHandler::class.java).resources.getString(R.string.remove_action_message, groupAction.name)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.remove_action)
            positiveButtonCallback = {
                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).removeGroupAction(groupAction.id!!).subscribe(
                            { `$`(StoreHandler::class.java).store.box(GroupAction::class.java).remove(groupAction) },
                            { `$`(DefaultAlerts::class.java).thatDidntWork() }
                    ))
                }
            show()
        }
    }
}

typealias OnGroupActionRepliedListener = (groupAction: GroupAction) -> Unit
