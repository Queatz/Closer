package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.GroupMemberResult;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
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
                                    .doOnNext($(StoreHandler.class).getStore().box(GroupMember.class)::put)
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
                new MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, R.string.add_an_action, () -> {
                    if (group != null) {
                        $(GroupActionHandler.class).addActionToGroup(group);
                    }
                })
        );
    }
}
