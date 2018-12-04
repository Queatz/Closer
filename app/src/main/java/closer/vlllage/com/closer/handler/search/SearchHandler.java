package closer.vlllage.com.closer.handler.search;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class SearchHandler extends PoolMember {

    private SearchGroupsAdapter searchGroupsAdapter;
    private String searchQuery;
    private List<Group> groupsCache;
    private EditText searchGroups;

    public void attach(EditText searchGroups, RecyclerView groupsRecyclerView) {
        this.searchGroups = searchGroups;
        searchGroupsAdapter = new SearchGroupsAdapter(this, (group, view) -> this.openGroup(group.getId(), view), this::createGroup);
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item_centered);

        groupsRecyclerView.setAdapter(searchGroupsAdapter);
        groupsRecyclerView.setLayoutManager(new GridLayoutManager(
                groupsRecyclerView.getContext(),
                2
        ));

        searchGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                showGroupsForQuery(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.isPublic, true);

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroups())
                .build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroups));

        showGroupsForQuery("");

        $(LocationHandler.class).getCurrentLocation(location -> {
            $(RefreshHandler.class).refreshGroupActions(new LatLng(location.getLatitude(), location.getLongitude()));
        });
    }

    public void openGroup(String groupId, View view) {
        if (groupId == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return;
        }
        $(GroupActivityTransitionHandler.class).showGroupMessages(view, groupId);
    }

    private void createGroup(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            return;
        }

        $(LocationHandler.class).getCurrentLocation(location -> {
            $(AlertHandler.class).make()
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.group_as_public, groupName))
                    .setLayoutResId(R.layout.create_public_group_modal)
                    .setTextView(R.id.input, about -> {
                        searchGroups.setText("");
                        Group group = $(StoreHandler.class).create(Group.class);
                        group.setName(groupName);
                        group.setAbout(about);
                        group.setPublic(true);
                        group.setLatitude(location.getLatitude());
                        group.setLongitude(location.getLongitude());
                        $(StoreHandler.class).getStore().box(Group.class).put(group);
                        $(SyncHandler.class).sync(group, groupId -> openGroup(groupId, null));
                    })
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_public_group))
                    .show();
        }, () -> $(DefaultAlerts.class).thatDidntWork($(ResourcesHandler.class).getResources().getString(R.string.location_is_needed)));
    }

    private void showGroupsForQuery(String searchQuery) {
        this.searchQuery = searchQuery.toLowerCase();
        searchGroupsAdapter.setCreatePublicGroupName(searchQuery.trim().isEmpty() ? null : searchQuery);

        if (this.groupsCache != null) {
            setGroups(this.groupsCache);
        }
    }

    private void setGroups(List<Group> allGroups) {
        this.groupsCache = allGroups;

        List<Group> groups = new ArrayList<>();
        for(Group group : allGroups) {
            if (group.getName() != null) {
                if (group.getName().toLowerCase().contains(searchQuery) ||
                    $(Val.class).of(group.getAbout(), "").toLowerCase().contains(searchQuery) ||
                        groupActionNamesContains(group, searchQuery)) {
                    groups.add(group);
                }
            }
        }

        searchGroupsAdapter.setGroups(groups);
    }

    private boolean groupActionNamesContains(Group group, String searchQuery) {
        List<GroupAction> groupActions = $(StoreHandler.class).getStore().box(GroupAction.class).query()
                .equal(GroupAction_.group, group.getId()).build().find();

        for (GroupAction groupAction : groupActions) {
            if (groupAction.getName().toLowerCase().contains(searchQuery)) {
                return true;
            }
        }

        return false;
    }
}
