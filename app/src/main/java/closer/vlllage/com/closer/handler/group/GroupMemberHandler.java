package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.GroupMemberResult;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.InstallShortcutHandler;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupMember;
import closer.vlllage.com.closer.store.models.GroupMember_;
import io.objectbox.android.AndroidScheduler;

public class GroupMemberHandler extends PoolMember {
    public void changeGroupSettings(Group group) {
        if (group == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }

        if (group.isPublic()) {
            $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupMember.class).query()
                    .equal(GroupMember_.group, group.getId())
                    .equal(GroupMember_.phone, $(PersistenceHandler.class).getPhoneId())
                    .build().subscribe().single().on(AndroidScheduler.mainThread()).observer(groupMembers -> {
                        if (groupMembers.isEmpty()) {
                            $(DisposableHandler.class).add($(ApiHandler.class).getGroupMember(group.getId())
                                    .map(GroupMemberResult::from)
                                    .doOnSuccess($(StoreHandler.class).getStore().box(GroupMember.class)::put)
                                    .subscribe(
                                            groupMember -> setupGroupMember(group, groupMember),
                                            error -> setupGroupMember(group, null)
                                    ));
                        } else {
                            setupGroupMember(group, groupMembers.get(0));
                        }
                    }));
        } else {
            $(MenuHandler.class).show(
                    new MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action, () -> {
                        $(GroupActionHandler.class).addActionToGroup(group);
                    }),
                    new MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut, () -> {
                        $(InstallShortcutHandler.class).installShortcut(group);
                    }),
                    new MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background, () -> {
                        $(PhysicalGroupUpgradeHandler.class).setBackground(group, updateGroup -> { });
                    }));
        }
    }

    private void setupGroupMember(Group group, GroupMember groupMember) {
        if (groupMember == null) {
            groupMember = new GroupMember();
            groupMember.setGroup(group.getId());
            groupMember.setPhone($(PersistenceHandler.class).getPhoneId());
        }

        @StringRes int subscribeText = groupMember.isSubscribed() ? R.string.unsubscribe : R.string.subscribe;
        @DrawableRes int subscribeIcon = groupMember.isSubscribed() ? R.drawable.ic_baseline_check_circle_24px : R.drawable.ic_baseline_check_circle_outline_24px;
        @StringRes int muteText = groupMember.isMuted() ? R.string.unmute_notifications : R.string.mute_notifications;
        @DrawableRes int muteIcon = groupMember.isMuted() ? R.drawable.ic_notifications_off_black_24dp : R.drawable.ic_notifications_none_black_24dp;

        final GroupMember updatedGroupMember = groupMember;
        $(MenuHandler.class).show(
                new MenuHandler.MenuOption(subscribeIcon, subscribeText, () -> {
                    updatedGroupMember.setSubscribed(!updatedGroupMember.isSubscribed());
                    $(SyncHandler.class).sync(updatedGroupMember);
                }),
                new MenuHandler.MenuOption(muteIcon, muteText, () -> {
                    updatedGroupMember.setMuted(!updatedGroupMember.isMuted());
                    $(SyncHandler.class).sync(updatedGroupMember);
                }),
                new MenuHandler.MenuOption(R.drawable.ic_person_add_black_24dp, R.string.join, () -> {
                    if (group != null) {
                        $(AlertHandler.class).make()
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.join_group))
                                .setPositiveButtonCallback(result -> $(GroupActionHandler.class).joinGroup(group))
                                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.join_group_title, group.getName()))
                                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.join_group_message))
                                .show();

                    }
                }).visible(!isCurrentUserMemberOf(group)),
                new MenuHandler.MenuOption(R.drawable.ic_share_black_24dp, R.string.share_group, () -> {
                    if (group != null) {
                        $(ShareActivityTransitionHandler.class).shareGroupToGroup(group.getId());
                    }
                }),
                new MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action, () -> {
                    if (group != null) {
                        $(GroupActionHandler.class).addActionToGroup(group);
                    }
                }),
                new MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.add_a_shortcut, () -> {
                    if (group != null) {
                        $(InstallShortcutHandler.class).installShortcut(group);
                    }
                }),
                new MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.update_background, () -> {
                    if (group != null) {
                        $(PhysicalGroupUpgradeHandler.class).setBackground(group, updateGroup -> { });
                    }
                }),
                new MenuHandler.MenuOption(R.drawable.ic_edit_black_24dp, R.string.edit_about_group, () -> {
                    if (group != null) {
                        $(AlertHandler.class).make()
                                .setTitle($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)))
                                .setLayoutResId(R.layout.create_public_group_modal)
                                .setTextView(R.id.input, about -> {
                                    $(PhysicalGroupUpgradeHandler.class).setAbout(group, about, updateGroup -> {});
                                })
                                .setOnAfterViewCreated((alert, view) -> ((TextView) view.findViewById(alert.getTextViewId())).setText(group.getAbout()))
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.edit_about_group))
                                .show();
                    }
                })
        );
    }

    private boolean isCurrentUserMemberOf(Group group) {
        if (group == null) return false;

        return $(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.groupId, group.getId())
                .equal(GroupContact_.contactId, $(PersistenceHandler.class).getPhoneId())
                .build()
                .count() > 0;
    }
}
