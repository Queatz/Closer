package closer.vlllage.com.closer.handler.group

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.widget.EditText
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.GroupMemberResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import io.objectbox.android.AndroidScheduler

class GroupMemberHandler : PoolMember() {
    fun changeGroupSettings(group: Group?) {
        if (group == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        if (group.isPublic) {
            `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupMember::class.java).query()
                    .equal(GroupMember_.group, group.id!!)
                    .equal(GroupMember_.phone, `$`(PersistenceHandler::class.java).phoneId)
                    .build().subscribe().single().on(AndroidScheduler.mainThread()).observer { groupMembers ->
                        if (groupMembers.isEmpty()) {
                            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getGroupMember(group.id!!)
                                    .map { GroupMemberResult.from(it) }
                                    .doOnSuccess { `$`(StoreHandler::class.java).store.box(GroupMember::class.java).put(it) }
                                    .subscribe(
                                            { groupMember -> setupGroupMember(group, groupMember) },
                                            { error -> setupGroupMember(group, null) }
                                    ))
                        } else {
                            setupGroupMember(group, groupMembers[0])
                        }
                    })
        } else {
            `$`(MenuHandler::class.java).show(
                    MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action) { `$`(GroupActionHandler::class.java).addActionToGroup(group) },
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) { `$`(InstallShortcutHandler::class.java).installShortcut(group) },
                    MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) { `$`(PhysicalGroupUpgradeHandler::class.java).setBackground(group) { updateGroup -> } })
        }
    }

    private fun setupGroupMember(group: Group?, groupMember: GroupMember?) {
        var groupMember = groupMember
        if (groupMember == null) {
            groupMember = GroupMember()
            groupMember.group = group!!.id
            groupMember.phone = `$`(PersistenceHandler::class.java).phoneId
        }

        @StringRes val subscribeText = if (groupMember.subscribed) R.string.unsubscribe else R.string.subscribe
        @DrawableRes val subscribeIcon = if (groupMember.subscribed) R.drawable.ic_baseline_check_circle_24px else R.drawable.ic_baseline_check_circle_outline_24px
        @StringRes val muteText = if (groupMember.muted) R.string.unmute_notifications else R.string.mute_notifications
        @DrawableRes val muteIcon = if (groupMember.muted) R.drawable.ic_notifications_off_black_24dp else R.drawable.ic_notifications_none_black_24dp

        val updatedGroupMember = groupMember
        `$`(MenuHandler::class.java).show(
                MenuHandler.MenuOption(subscribeIcon, subscribeText) {
                    updatedGroupMember.subscribed = !updatedGroupMember.subscribed
                    `$`(SyncHandler::class.java).sync(updatedGroupMember)
                },
                MenuHandler.MenuOption(muteIcon, muteText) {
                    updatedGroupMember.muted = !updatedGroupMember.muted
                    `$`(SyncHandler::class.java).sync(updatedGroupMember)
                },
                MenuHandler.MenuOption(R.drawable.ic_person_add_black_24dp, R.string.join) {
                    if (group != null) {
                        `$`(AlertHandler::class.java).make().apply {
                            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.join_group)
                            positiveButtonCallback = { result -> `$`(GroupActionHandler::class.java).joinGroup(group) }
                            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.join_group_title, group.name)
                            message = `$`(ResourcesHandler::class.java).resources.getString(R.string.join_group_message)
                            show()
                        }

                    }
                }.visible(!isCurrentUserMemberOf(group)),
                MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, R.string.share_group) {
                    if (group != null) {
                        `$`(ShareActivityTransitionHandler::class.java).shareGroupToGroup(group.id!!)
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) {
                    if (group != null) {
                        `$`(PhysicalGroupUpgradeHandler::class.java).setBackground(group) { updateGroup -> }
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.edit_about_group) {
                    if (group != null) {
                        `$`(AlertHandler::class.java).make().apply {
                            title = `$`(Val::class.java).of(group.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
                            layoutResId = R.layout.create_public_group_modal
                            textViewId = R.id.input
                            onTextViewSubmitCallback = { about -> `$`(PhysicalGroupUpgradeHandler::class.java).setAbout(group, about) { updateGroup -> } }
                            onAfterViewCreated = { alert, view -> view.findViewById<EditText>(alert.textViewId!!).setText(group.about!!) }
                            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.edit_about_group)
                            show()
                        }
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action) {
                    if (group != null) {
                        `$`(GroupActionHandler::class.java).addActionToGroup(group)
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) {
                    if (group != null) {
                        `$`(InstallShortcutHandler::class.java).installShortcut(group)
                    }
                }
        )
    }

    private fun isCurrentUserMemberOf(group: Group?): Boolean {
        return if (group == null) false else `$`(StoreHandler::class.java).store.box(GroupContact::class.java).query()
                .equal(GroupContact_.groupId, group.id!!)
                .equal(GroupContact_.contactId, `$`(PersistenceHandler::class.java).phoneId)
                .build()
                .count() > 0

    }
}
