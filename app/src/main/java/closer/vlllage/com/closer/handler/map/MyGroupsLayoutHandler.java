package closer.vlllage.com.closer.handler.map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.AppShortcutsHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActionBarButton;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.MyGroupsAdapter;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;

public class MyGroupsLayoutHandler extends PoolMember {
    private ViewGroup myGroupsLayout;
    private MyGroupsAdapter myGroupsAdapter;
    private RecyclerView myGroupsRecyclerView;
    private View containerView;

    private boolean hasSetGroupShortcuts;

    public void attach(ViewGroup myGroupsLayout) {
        this.myGroupsLayout = myGroupsLayout;
        myGroupsRecyclerView = myGroupsLayout.findViewById(R.id.myGroupsRecyclerView);
        myGroupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                myGroupsRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        myGroupsAdapter = new MyGroupsAdapter(this);
        $(MyGroupsLayoutActionsHandler.class).attach(myGroupsAdapter);
        myGroupsRecyclerView.setAdapter(myGroupsAdapter);
        $(DisposableHandler.class).add($(StoreHandler.class).getStore().box(Group.class).query()
                .notEqual(Group_.isPublic, true)
                .sort($(SortHandler.class).sortGroups())
                .build().subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroups));


        List<GroupActionBarButton> endActions = new ArrayList<>();
        endActions.add(new GroupActionBarButton(
                $(ResourcesHandler.class).getResources().getString(R.string.add_new_private_group),
                view -> $(AlertHandler.class).make()
                                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_group))
                                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_new_private_group))
                                .setLayoutResId(R.layout.create_group_modal)
                                .setTextView(R.id.input, this::createGroup)
                                .show(),
                null,
                R.drawable.clickable_blue_light).setIcon(R.drawable.ic_group_add_black_24dp));
        endActions.add(new GroupActionBarButton(
                $(ResourcesHandler.class).getResources().getString(R.string.random_suggestion),
                view -> $(SuggestionHandler.class).shuffle(),
                null,
                R.drawable.clickable_green_light

        ).setIcon(R.drawable.ic_shuffle_black_24dp));
        endActions.add(new GroupActionBarButton(
                $(ResourcesHandler.class).getResources().getString(R.string.settings),
                view -> $(MapActivityHandler.class).goToScreen(MapsActivity.EXTRA_SCREEN_SETTINGS),
                null,
                R.drawable.clickable_accent

        ).setIcon(R.drawable.ic_settings_black_24dp));
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
        if (!hasSetGroupShortcuts) {
            $(AppShortcutsHandler.class).setGroupShortcuts(groups);
            hasSetGroupShortcuts = true;
        }
        myGroupsAdapter.setGroups(groups);
    }

    public int getHeight() {
        return myGroupsLayout.getMeasuredHeight();
    }

    public void showBottomPadding(boolean showBottomPadding) {
        containerView.setPadding(
                containerView.getPaddingLeft(),
                containerView.getPaddingTop(),
                containerView.getPaddingRight(),
                showBottomPadding ? $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeekHeight) : 0
        );
    }

    public void setContainerView(View containerView) {
        this.containerView = containerView;
    }

    public View getContainer() {
        return containerView;
    }
}
