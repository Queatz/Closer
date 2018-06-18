package closer.vlllage.com.closer.handler.map;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.group.GroupActionBarButton;
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SystemSettingsHandler;
import closer.vlllage.com.closer.handler.helpers.ToastHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;

public class MyGroupsLayoutActionsHandler extends PoolMember {

    private MyGroupsAdapter myGroupsAdapter;

    private final List<GroupActionBarButton> actions = new ArrayList<>();

    private GroupActionBarButton verifyYourNumberButton;
    private GroupActionBarButton allowPermissionsButton;
    private GroupActionBarButton unmuteNotificationsButton;
    private GroupActionBarButton showHelpButton;
    private GroupActionBarButton setMyName;

    private GroupActionBarButtonHandle verifyYourNumberButtonHandle = new GroupActionBarButtonHandle() {
        @Override
        public void set() {
            String action = $(ResourcesHandler.class).getResources().getString(R.string.verify_your_number);
            verifyYourNumberButton = new GroupActionBarButton(action, view -> $(VerifyNumberHandler.class).verify());
        }

        @Override
        public GroupActionBarButton get() {
            return verifyYourNumberButton;
        }
    };

    private GroupActionBarButtonHandle allowPermissionsButtonHandle = new GroupActionBarButtonHandle() {
        @Override
        public void set() {
            String action = $(ResourcesHandler.class).getResources().getString(R.string.use_your_location);
            allowPermissionsButton = new GroupActionBarButton(action, view -> $(AlertHandler.class).make()
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enable_location_permission))
                    .setMessage($(ResourcesHandler.class).getResources().getString(R.string.enable_location_permission_rationale))
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.open_settings))
                    .setPositiveButtonCallback(alertResult -> $(SystemSettingsHandler.class).showSystemSettings())
                    .show());
        }

        @Override
        public GroupActionBarButton get() {
            return allowPermissionsButton;
        }
    };

    private GroupActionBarButtonHandle unmuteNotificationsButtonHandle = new GroupActionBarButtonHandle() {
        @Override
        public void set() {
            String action = $(ResourcesHandler.class).getResources().getString(R.string.unmute_notifications);
            unmuteNotificationsButton = new GroupActionBarButton(action, view -> {
                $(PersistenceHandler.class).setIsNotificationsPaused(false);
                $(ToastHandler.class).show(R.string.notifications_on);
                actions.remove(unmuteNotificationsButton);
                myGroupsAdapter.setActions(actions);
            });
        }

        @Override
        public GroupActionBarButton get() {
            return unmuteNotificationsButton;
        }
    };

    private GroupActionBarButtonHandle showHelpButtonHandle = new GroupActionBarButtonHandle() {
        @Override
        public void set() {
            String action = $(ResourcesHandler.class).getResources().getString(R.string.show_help);
            showHelpButton = new GroupActionBarButton(action, view -> $(DefaultAlerts.class).longMessage(null, R.string.help_message), view -> {
                $(PersistenceHandler.class).setIsHelpHidden(true);
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.you_hid_the_help_bubble))
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                        .show();
                showHelpButton(false);
            }, R.drawable.clickable_green_light);
        }

        @Override
        public GroupActionBarButton get() {
            return showHelpButton;
        }
    };

    private GroupActionBarButtonHandle setMyNameHandle = new GroupActionBarButtonHandle() {
        @Override
        public void set() {
            String action = $(ResourcesHandler.class).getResources().getString(R.string.set_my_name);
            setMyName = new GroupActionBarButton(action, view -> {
                $(SetNameHandler.class).modifyName(name -> {
                    if (!$(Val.class).isEmpty(name)) {
                        showSetMyName(false);
                    }
                }, false);
            });
        }

        @Override
        public GroupActionBarButton get() {
            return setMyName;
        }
    };

    public void attach(MyGroupsAdapter myGroupsAdapter) {
        this.myGroupsAdapter = myGroupsAdapter;
    }

    void showVerifyMyNumber(boolean show) {
        show(verifyYourNumberButtonHandle, show, -1);
    }

    void showAllowLocationPermissionsInSettings(boolean show) {
        show(allowPermissionsButtonHandle, show, 0);
    }

    void showUnmuteNotifications(boolean show) {
        show(unmuteNotificationsButtonHandle, show, 0);
    }

    void showHelpButton(boolean show) {
        show(showHelpButtonHandle, show, 0);
    }

    void showSetMyName(boolean show) {
        show(setMyNameHandle, show, 0);
    }

    private void show(GroupActionBarButtonHandle handle, boolean show, int position) {
        if (show) {
            if (handle.get() != null) {
                return;
            }

            handle.set();
            if (position < 0) {
                actions.add(handle.get());
            } else {
                actions.add(position, handle.get());
            }
        } else {
            if (handle.get() == null) {
                return;
            }

            actions.remove(handle.get());
        }

        myGroupsAdapter.setActions(actions);
    }

    interface GroupActionBarButtonHandle {
        void set();
        GroupActionBarButton get();
    }
}
