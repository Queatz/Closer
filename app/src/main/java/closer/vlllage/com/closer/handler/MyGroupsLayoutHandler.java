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
import closer.vlllage.com.closer.store.models.Group_;
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
                .notEqual(Group_.isPublic, true)
                .sort($(SortHandler.class).sortGroups())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroups));


        List<GroupActionBarButton> endActions = new ArrayList<>();
        endActions.add(new GroupActionBarButton(
                $(ResourcesHandler.class).getResources().getString(R.string.add_new_group),
                view -> $(AlertHandler.class).make()
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_group))
                                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.create_a_new_group))
                                .setLayoutResId(R.layout.create_group_modal)
                                .setTextView(R.id.input, this::createGroup)
                                .show(),
                null,
                R.drawable.clickable_blue_light).setIcon(R.drawable.ic_group_add_black_24dp));
        endActions.add(new GroupActionBarButton(
                $(ResourcesHandler.class).getResources().getString(R.string.search_public_groups),
                view -> $(SearchActivityHandler.class).show(view),
                null,
                R.drawable.clickable_green_light

        ).setIcon(R.drawable.ic_search_black_24dp));
        myGroupsAdapter.setEndActions(endActions);
    }

    private void createGroup(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Group group = $(StoreHandler.class).create(Group.class);
        group.setName(name);
        $(StoreHandler.class).getStore().box(Group.class).put(group);
        $(SyncHandler.class).sync(group, groupId ->
                $(GroupActivityTransitionHandler.class).showGroupMessages(null, groupId));
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
            verifyYourNumberButton = new GroupActionBarButton(action, view -> $(VerifyNumberHandler.class).verify());
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
            allowPermissionsButton = new GroupActionBarButton(action, view -> $(AlertHandler.class).make()
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
            showHelpButton = new GroupActionBarButton(action, view -> $(DefaultAlerts.class).longMessage(null, R.string.help_message), view -> {
                $(PersistenceHandler.class).setIsHelpHidden(true);
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.you_hid_the_help_bubble))
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                        .show();
                showHelpButton(false);
            }, R.drawable.clickable_green_light);
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
