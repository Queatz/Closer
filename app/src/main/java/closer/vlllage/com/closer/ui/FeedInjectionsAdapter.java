package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.model.CameraPosition;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.SearchGroupHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.handler.search.SearchActivityHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static closer.vlllage.com.closer.pool.Pool.tempPool;

public class FeedInjectionsAdapter extends PoolRecyclerAdapter<FeedInjectionsAdapter.ViewHolder> implements CombinedRecyclerAdapter.PrioritizedAdapter {

    public FeedInjectionsAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResId;

        switch (viewType) {
            case 0:
                layoutResId = R.layout.feed_item_public_groups;
                break;
            case 1:
                layoutResId = R.layout.feed_item_card;
                break;
            default:
                throw new IllegalStateException("Unimplemented feed injection item type");
        }

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pool = tempPool();
        holder.pool.$(ApplicationHandler.class).setApp($(ApplicationHandler.class).getApp());
        holder.pool.$(ActivityHandler.class).setActivity($(ActivityHandler.class).getActivity());

        switch (getItemViewType(position)) {
            case 0:
                RecyclerView groupsRecyclerView = holder.itemView.findViewById(R.id.publicGroupsRecyclerView);
                EditText searchGroups = holder.itemView.findViewById(R.id.searchGroups);

                SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter(holder.pool.$(PoolMember.class), (group, view) -> this.openGroup(group.getId(), view), this::createGroup);
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
                        holder.pool.$(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, searchGroups.getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                holder.pool.$(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, searchGroups.getText().toString());

                float distance = .12f;

                Consumer<CameraPosition> cameraPositionCallback = cameraPosition -> {
                    QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                            .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                            .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                            .equal(Group_.isPublic, true)
                            .notEqual(Group_.name, "");

                    holder.pool.$(DisposableHandler.class).add(queryBuilder
                            .sort($(SortHandler.class).sortGroups())
                            .build()
                            .subscribe()
                            .on(AndroidScheduler.mainThread())
                            .single()
                            .observer(groups -> {
                                    holder.pool.$(SearchGroupHandler.class).setGroups(groups);
                            }));
                };

                try {
                    cameraPositionCallback.accept(CameraPosition.fromLatLngZoom($(MapHandler.class).getCenter(), $(MapHandler.class).getZoom()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                holder.pool.$(DisposableHandler.class).add($(MapHandler.class).onMapIdleObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cameraPositionCallback));

                holder.itemView.findViewById(R.id.action).setOnClickListener(view -> $(SearchActivityHandler.class).show(view));
                break;
            case 1:

                break;
            default:
                throw new IllegalStateException("Unimplemented feed injection item type");
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.pool.end();
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemPriority(int position) {
        return position * 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TempPool pool;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
