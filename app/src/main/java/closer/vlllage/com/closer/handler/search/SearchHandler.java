package closer.vlllage.com.closer.handler.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.CircularRevealActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.LocationHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.handler.SortHandler;
import closer.vlllage.com.closer.handler.SyncHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
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
        searchGroupsAdapter = new SearchGroupsAdapter(this, group -> this.openGroup(group.getId()), this::createGroup);

        groupsRecyclerView.setAdapter(searchGroupsAdapter);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                groupsRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
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
    }

    private void openGroup(String groupId) {
        ((CircularRevealActivity) $(ActivityHandler.class).getActivity()).finish(() -> $(GroupActivityTransitionHandler.class).showGroupMessages(null, groupId));

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
                        $(SyncHandler.class).sync(group, this::openGroup);
                    })
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_public_group))
                    .show();
        }, () -> $(DefaultAlerts.class).thatDidntWork($(ResourcesHandler.class).getResources().getString(R.string.location_is_needed)));
    }

    private void showGroupsForQuery(String searchQuery) {
        this.searchQuery = searchQuery;
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
                if (group.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    groups.add(group);
                }
            }
        }

        searchGroupsAdapter.setGroups(groups);
    }
}
