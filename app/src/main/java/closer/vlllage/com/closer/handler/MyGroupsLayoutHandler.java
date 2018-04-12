package closer.vlllage.com.closer.handler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.GroupActionBarButton;
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;

public class MyGroupsLayoutHandler extends PoolMember {
    private ViewGroup myGroupsLayout;
    private MyGroupsAdapter myGroupsAdapter;
    private final List<GroupActionBarButton> actions = new ArrayList<>();

    private GroupActionBarButton verifyYourNumberButton;
    private GroupActionBarButton allowPermissionsButton;
    private GroupActionBarButton showHelpButton;

    public void attach(ViewGroup myGroupsLayout) {
        this.myGroupsLayout = myGroupsLayout;
        RecyclerView myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView);
        myGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                myGroupsRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        myGroupsAdapter = new MyGroupsAdapter(this);
        myGroupsRecyclerView.setAdapter(myGroupsAdapter);
        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(Group.class).query()
                .sort($(SortHandler.class).sortGroups())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroups));
    }

    private void setGroups(List<Group> groups) {
        $(AppShortcutsHandler.class).setGroupShortcuts(groups);
        myGroupsAdapter.setGroups(groups);
    }

    public int getHeight() {
        return myGroupsLayout.getMeasuredHeight();
    }

    public void showVerifyMyNumber(boolean show) {
        if (show) {
            if (verifyYourNumberButton != null) {
                return;
            }

            String action = $(ResourcesHandler.class).getResources().getString(R.string.verify_your_number);
            verifyYourNumberButton = new GroupActionBarButton(action, () -> $(VerifyNumberHandler.class).verify());
            actions.add(verifyYourNumberButton);
        } else {
            if (verifyYourNumberButton == null) {
                return;
            }

            actions.remove(verifyYourNumberButton);
        }

        myGroupsAdapter.setActions(actions);
    }

    public void showAllowLocationPermissionsInSettings(boolean show) {
        if (show) {
            if (allowPermissionsButton != null) {
                return;
            }

            String action = $(ResourcesHandler.class).getResources().getString(R.string.use_your_location);
            allowPermissionsButton = new GroupActionBarButton(action, () -> $(AlertHandler.class).make()
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enable_location_permission))
                    .setMessage($(ResourcesHandler.class).getResources().getString(R.string.enable_location_permission_rationale))
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.open_settings))
                    .setPositiveButtonCallback(alertResult -> $(SystemSettingsHandler.class).showSystemSettings())
                    .show());
            actions.add(0, allowPermissionsButton);
        } else {
            if (allowPermissionsButton == null) {
                return;
            }

            actions.remove(allowPermissionsButton);
        }

        myGroupsAdapter.setActions(actions);
    }

    public void showHelpButton(boolean show) {
        if (show) {
            if (showHelpButton != null) {
                return;
            }

            String action = $(ResourcesHandler.class).getResources().getString(R.string.show_help);
            showHelpButton = new GroupActionBarButton(action, () -> $(AlertHandler.class).make()
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.help))
                    .setMessage($(ResourcesHandler.class).getResources().getString(R.string.help_message))
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                    .show(), () -> {
                $(PersistenceHandler.class).setIsHelpHidden(true);
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.you_hid_the_help_bubble))
                        .show();
                showHelpButton(false);
            });
            actions.add(0, showHelpButton);
        } else {
            if (showHelpButton == null) {
                return;
            }

            actions.remove(showHelpButton);
        }

        myGroupsAdapter.setActions(actions);
    }
}
