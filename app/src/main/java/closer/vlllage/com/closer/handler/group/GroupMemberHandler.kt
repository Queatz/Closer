package closer.vlllage.com.closer.handler.group

import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.GroupMember
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers

class GroupMemberHandler constructor(private val on: On) {
    fun changeGroupSettings(group: Group?) {
        if (group == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        when {
            group.isPublic -> {
                on<DataHandler>().getGroupMember(group.id!!).subscribe(
                        { groupMember -> setupGroupMember(group, groupMember) },
                        { setupGroupMember(group, null) }
                ).also { on<DisposableHandler>().add(it) }
            }
            else -> {
                on<MenuHandler>().show(
                        MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action) { on<GroupActionHandler>().addActionToGroup(group) },
                        MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut) { on<InstallShortcutHandler>().installShortcut(group) },
                        MenuHandler.MenuOption(R.drawable.ic_chat_black_24dp, R.string.change_your_group_status) {
                            on<DataHandler>().getGroupContact(group.id!!, on<PersistenceHandler>().phoneId!!)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ groupContact ->
                                        on<DefaultInput>().show(
                                                R.string.your_group_status,
                                                buttonRes = R.string.update,
                                                prefill = groupContact.status
                                        ) {
                                            on<GroupContactHandler>().updateGroupStatus(groupContact, it)
                                        }
                                    }, {
                                        on<DefaultAlerts>().thatDidntWork()
                                    })
                        }.visible(group.direct),
                        MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.change_your_group_photo) {
                            on<DataHandler>().getGroupContact(group.id!!, on<PersistenceHandler>().phoneId!!)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ groupContact ->
                                        on<DefaultMenus>().uploadPhoto { photoId ->
                                            val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                                            on<GroupContactHandler>().updateGroupPhoto(groupContact, photo)
                                        }
                                    }, {
                                        on<DefaultAlerts>().thatDidntWork()
                                    })
                        }.visible(group.direct),
                        MenuHandler.MenuOption(R.drawable.ic_visibility_black_24dp, R.string.view_background) {
                            group.photo?.let { on<PhotoActivityTransitionHandler>().show(null, it) }
                        }.visible(group.photo != null),
                        MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) { on<PhysicalGroupUpgradeHandler>().setBackground(group) { updateGroup -> } },
                        MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.update_description) {
                            on<AlertHandler>().make().apply {
                                title = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
                                layoutResId = R.layout.create_public_group_modal
                                textViewId = R.id.input
                                onTextViewSubmitCallback = { about -> on<PhysicalGroupUpgradeHandler>().setAbout(group, about) { updateGroup -> } }
                                onAfterViewCreated = { alert, view ->
                                    view.findViewById<EditText>(alert.textViewId!!).setText(group.about
                                            ?: "")
                                }
                                positiveButton = on<ResourcesHandler>().resources.getString(R.string.update_description)
                                show()
                            }
                        },
                        MenuHandler.MenuOption(R.drawable.ic_baseline_alarm_24, R.string.edit_reminders) {
                            on<DataHandler>().getEvent(group.eventId!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
                                on<EventHandler>().editReminders(it)
                            }, {
                                on<DefaultAlerts>().thatDidntWork()
                            })
                        }.visible(group.hasEvent() /* todo only show for event owner */),
                        MenuHandler.MenuOption(R.drawable.ic_refresh_black_24dp, R.string.host_again) {
                            on<DataHandler>().getEvent(group.eventId!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
                                on<EventHandler>().hostAgain(it)
                            }, {
                                on<DefaultAlerts>().thatDidntWork()
                            })
                        }.visible(group.hasEvent()),
                        MenuHandler.MenuOption(R.drawable.ic_more_horiz_black_24dp, R.string.more_options) {
                            on<MenuHandler>().show(MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.rename_group) {
                                on<DefaultInput>().show(R.string.rename_group, prefill = group.name) { name ->
                                    on<ApiHandler>().renameGroup(group.id!!, name).observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({
                                            if (it.success) {
                                                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(if (group.hasEvent()) R.string.event_renamed else R.string.group_renamed, name))
                                            } else {
                                                on<DefaultAlerts>().thatDidntWork()
                                            }
                                        }, {
                                            on<DefaultAlerts>().thatDidntWork()
                                        })
                                }
                            })
                        },
                        MenuHandler.MenuOption(R.drawable.ic_visibility_black_24dp, R.string.unhide_from_contacts) {
                            on<HideHandler>().unhide(group)
                        }.visible(group.direct && on<HideHandler>().isHidden(group.id!!))
                )
            }
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
                    if (updatedGroupMember.subscribed) {
                        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.subscribed_message, group?.name
                                ?: on<ResourcesHandler>().resources.getString(R.string.unknown)), long = true)
                    } else {
                        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.unsubscribed_message, group?.name
                                ?: on<ResourcesHandler>().resources.getString(R.string.unknown)), long = true)
                    }
                    on<SyncHandler>().sync(updatedGroupMember)
                },
                MenuHandler.MenuOption(muteIcon, muteText) {
                    updatedGroupMember.muted = !updatedGroupMember.muted
                    on<SyncHandler>().sync(updatedGroupMember)

                    on<ToastHandler>().show(when (updatedGroupMember.muted) {
                        true -> R.string.notifications_muted
                        false -> R.string.notifications_unmuted
                    })
                },
                MenuHandler.MenuOption(R.drawable.ic_person_add_black_24dp, if (group?.hasEvent() == true) R.string.join_this_event else R.string.join_this_group) {
                    if (group != null) {
                        join(group)
                    }
                }.visible(!isCurrentUserMemberOf(group) && group?.hasPhone() != true),
                MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, R.string.share_group) {
                    if (group != null) {
                        on<ShareActivityTransitionHandler>().shareGroupToGroup(group.id!!)
                    }
                }.visible(group?.hasPhone() != true),
                MenuHandler.MenuOption(R.drawable.ic_visibility_black_24dp, R.string.view_background) {
                    group?.photo?.let { on<PhotoActivityTransitionHandler>().show(null, it) }
                }.visible(group?.photo != null),
                MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background) {
                    if (group != null) {
                        on<PhysicalGroupUpgradeHandler>().setBackground(group) { updateGroup -> }
                    }
                },
                MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.update_description) {
                    if (group != null) {
                        on<AlertHandler>().make().apply {
                            title = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
                            layoutResId = R.layout.create_public_group_modal
                            textViewId = R.id.input
                            onTextViewSubmitCallback = { about -> on<PhysicalGroupUpgradeHandler>().setAbout(group, about) { updateGroup -> } }
                            onAfterViewCreated = { alert, view -> view.findViewById<EditText>(alert.textViewId!!).setText(group.about ?: "") }
                            positiveButton = on<ResourcesHandler>().resources.getString(R.string.update_description)
                            show()
                        }
                    }
                }.visible(group?.hasPhone() != true),
                MenuHandler.MenuOption(R.drawable.ic_baseline_alarm_24, R.string.edit_reminders) {
                    on<DataHandler>().getEvent(group!!.eventId!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        on<EventHandler>().editReminders(it)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    })
                }.visible(group?.hasEvent() == true /* todo only show for event owner */),
                MenuHandler.MenuOption(R.drawable.ic_refresh_black_24dp, R.string.host_again) {
                    on<DataHandler>().getEvent(group!!.eventId!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
                        on<EventHandler>().hostAgain(it)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    })
                }.visible(group?.hasEvent() == true),
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

    fun mute(group: Group?, mute: Boolean) {
        if (group == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        on<DataHandler>().getGroupMember(group.id!!).subscribe(
                {
                    it.muted = mute
                    on<SyncHandler>().sync(it)

                    on<ToastHandler>().show(when (it.muted) {
                        true -> R.string.notifications_muted
                        false -> R.string.notifications_unmuted
                    })
                },
                {
                    on<DefaultAlerts>().thatDidntWork()
                }
        ).also { on<DisposableHandler>().add(it) }
    }

    fun join(group: Group) {
        on<AlertHandler>().make().apply {
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.join_group_title, group.name)
            positiveButtonCallback = { on<GroupActionHandler>().joinGroup(group) }
            title = on<ResourcesHandler>().resources.getString(if (group.hasEvent()) R.string.join_event else R.string.join_group)
            message = on<ResourcesHandler>().resources.getString(if (group.hasEvent()) R.string.join_event_message else R.string.join_group_message)
            show()
        }
    }

    fun isCurrentUserMemberOf(group: Group?): Boolean {
        return if (group == null) false else on<StoreHandler>().store.box(GroupContact::class).query()
                .equal(GroupContact_.groupId, group.id!!)
                .equal(GroupContact_.contactId, on<PersistenceHandler>().phoneId!!)
                .build()
                .count() > 0
    }
}
