package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.search.SearchActivityHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class FeedInjectionsAdapter extends PoolRecyclerAdapter<RecyclerView.ViewHolder> implements CombinedRecyclerAdapter.PrioritizedAdapter {

    public FeedInjectionsAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 0:
                RecyclerView groupsRecyclerView = holder.itemView.findViewById(R.id.publicGroupsRecyclerView);

                SearchGroupsAdapter searchGroupsAdapter = new SearchGroupsAdapter($pool(), (group, view) -> this.openGroup(group.getId(), view), this::createGroup);
                searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item);

                groupsRecyclerView.setAdapter(searchGroupsAdapter);
                groupsRecyclerView.setLayoutManager(new LinearLayoutManager(
                        groupsRecyclerView.getContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                ));

                QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                        .equal(Group_.isPublic, true)
                        .notEqual(Group_.physical, true);

                $(DisposableHandler.class).add(queryBuilder
                        .sort($(SortHandler.class).sortGroups())
                        .build()
                        .subscribe().on(AndroidScheduler.mainThread())
                        .observer(searchGroupsAdapter::setGroups));

                holder.itemView.findViewById(R.id.action).setOnClickListener(view -> $(SearchActivityHandler.class).show(view));
                break;
            case 1:

                break;
            default:
                throw new IllegalStateException("Unimplemented feed injection item type");
        }
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
        return 2;
    }

    @Override
    public int getItemPriority(int position) {
        return position * 10;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
