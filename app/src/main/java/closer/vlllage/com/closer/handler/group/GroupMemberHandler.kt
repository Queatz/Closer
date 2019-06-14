package closer.vlllage.com.closer.handler.group

import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.GroupMemberResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler

class GroupMemberHandler constructor(private val on: On) {
    fun changeGroupSettings(group: Group?) {
        if (group == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        if (group.hasPhone()) {
            on<MenuHandler>().run {
                show(
                        MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) { on<InstallShortcutHandler>().installShortcut(group) }
                )
            }
        } else if (group.isPublic) {
            on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class).query()
                    .equal(GroupMember_.group, group.id!!)
                    .equal(GroupMember_.phone, on<PersistenceHandler>().phoneId!!)
                    .build().subscribe().single().on(AndroidScheduler.mainThread()).observer { groupMembers ->
                        if (groupMembers.isEmpty()) {
                            on<DisposableHandler>().add(on<ApiHandler>().getGroupMember(group.id!!)
                                    .map { GroupMemberResult.from(it) }
                                    .doOnSuccess { on<StoreHandler>().store.box(GroupMember::class).put(it) }
                                    .subscribe(
                                            { groupMember -> setupGroupMember(group, groupMember) },
                                            { error -> setupGroupMember(group, null) }
                                    ))
                        } else {
                            setupGroupMember(group, groupMembers[0])
                        }
                    })
        } else {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action) { on<GroupActionHandler>().addActionToGroup(group) },
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) { on<InstallShortcutHandler>().installShortcut(group) },
                    MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) { on<PhysicalGroupUpgradeHandler>().setBackground(group) { updateGroup -> } })
        }
    }

    private fun setupGroupMember(group: Group?, groupMember: GroupMember?) {
        var groupMember = groupMember
        if (groupMember == null) {
            groupMember = GroupMember()
            groupMember.group = group!!.id
            groupMember.phone = on<PersistenceHandler>().phoneId
        }

        @StringRes val subscribeText = if (groupMember.subscribed) R.string.unsubscribe else R.string.subscribe
        @DrawableRes val subscribeIcon = if (groupMember.subscribed) R.drawable.ic_baseline_check_circle_24px else R.drawable.ic_baseline_check_circle_outline_24px
        @StringRes val muteText = if (groupMember.muted) R.string.unmute_notifications else R.string.mute_notifications
        @DrawableRes val muteIcon = if (groupMember.muted) R.drawable.ic_notifications_off_black_24dp else R.drawable.ic_notifications_none_black_24dp

        val updatedGroupMember = groupMember
        on<MenuHandler>().show(
                MenuHandler.MenuOption(subscribeIcon, subscribeText) {
                    updatedGroupMember.subscribed = !updatedGroupMember.subscribed
                    on<SyncHandler>().sync(updatedGroupMember)
                },
                MenuHandler.MenuOption(muteIcon, muteText) {
                    updatedGroupMember.muted = !updatedGroupMember.muted
                    on<SyncHandler>().sync(updatedGroupMember)
                },
                MenuHandler.MenuOption(R.drawable.ic_person_add_black_24dp, R.string.join) {
                    if (group != null) {
                        on<AlertHandler>().make().apply {
                            positiveButton = on<ResourcesHandler>().resources.getString(R.string.join_group)
                            positiveButtonCallback = { result -> on<GroupActionHandler>().joinGroup(group) }
                            title = on<ResourcesHandler>().resources.getString(R.string.join_group_title, group.name)
                            message = on<ResourcesHandler>().resources.getString(R.string.join_group_message)
                            show()
                        }

                    }
                }.visible(!isCurrentUserMemberOf(group)),
                MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, R.string.share_group) {
                    if (group != null) {
                        on<ShareActivityTransitionHandler>().shareGroupToGroup(group.id!!)
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) {
                    if (group != null) {
                        on<PhysicalGroupUpgradeHandler>().setBackground(group) { updateGroup -> }
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.edit_about_group) {
                    if (group != null) {
                        on<AlertHandler>().make().apply {
                            title = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
                            layoutResId = R.layout.create_public_group_modal
                            textViewId = R.id.input
                            onTextViewSubmitCallback = { about -> on<PhysicalGroupUpgradeHandler>().setAbout(group, about) { updateGroup -> } }
                            onAfterViewCreated = { alert, view -> view.findViewById<EditText>(alert.textViewId!!).setText(group.about!!) }
                            positiveButton = on<ResourcesHandler>().resources.getString(R.string.edit_about_group)
                            show()
                        }
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action) {
                    if (group != null) {
                        on<GroupActionHandler>().addActionToGroup(group)
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) {
                    if (group != null) {
                        on<InstallShortcutHandler>().installShortcut(group)
                    }
                }
        )
    }

    private fun isCurrentUserMemberOf(group: Group?): Boolean {
        return if (group == null) false else on<StoreHandler>().store.box(GroupContact::class).query()
                .equal(GroupContact_.groupId, group.id!!)
                .equal(GroupContact_.contactId, on<PersistenceHandler>().phoneId!!)
                .build()
                .count() > 0

    }
}
