package closer.vlllage.com.closer.handler.feed;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.CameraPosition;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.SearchGroupHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.handler.search.SearchActivityHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class PublicGroupFeedItemHandler extends PoolMember {
    public void attach(View itemView) {
        RecyclerView groupsRecyclerView = itemView.findViewById(R.id.publicGroupsRecyclerView);
        EditText searchGroups = itemView.findViewById(R.id.searchGroups);

        SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter($(PoolMember.class), (group, view) -> openGroup(group.getId(), view), this::createGroup);
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item);

        groupsRecyclerView.setAdapter(searchGroupsAdapter);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                groupsRecyclerView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        searchGroups.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                $(KeyboardHandler.class).showViewAboveKeyboard(view);
            }
        });

        searchGroups.setOnClickListener(view -> $(KeyboardHandler.class).showViewAboveKeyboard(view));

        searchGroups.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                $(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, searchGroups.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        $(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, searchGroups.getText().toString());

        float distance = .12f;

        Consumer<CameraPosition> cameraPositionCallback = cameraPosition -> {
            QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                    .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                    .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                    .equal(Group_.isPublic, true)
                    .notEqual(Group_.name, "");

            $(DisposableHandler.class).add(queryBuilder
                    .sort($(SortHandler.class).sortGroups())
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .single()
                    .observer(groups -> {
                        $(SearchGroupHandler.class).setGroups(groups);
                        $(TimerHandler.class).post(() -> groupsRecyclerView.scrollBy(0, 0));
                    }));
        };

        $(DisposableHandler.class).add($(MapHandler.class).onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPositionCallback));

        itemView.findViewById(R.id.action).setOnClickListener(view -> $(SearchActivityHandler.class).show(view));
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

    public void openGroup(String groupId, View view) {
        $(GroupActivityTransitionHandler.class).showGroupMessages(view, groupId);
    }

}
