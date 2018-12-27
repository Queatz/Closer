package closer.vlllage.com.closer.handler.search;

import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.group.GroupActionAdapter;
import closer.vlllage.com.closer.handler.group.GroupMemberHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import io.objectbox.android.AndroidScheduler;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import static closer.vlllage.com.closer.pool.Pool.tempPool;

public class SearchGroupsAdapter extends PoolRecyclerAdapter<SearchGroupsAdapter.SearchGroupsViewHolder> {

    private String createPublicGroupName = null;
    private final List<Group> groups = new ArrayList<>();
    private OnGroupClickListener onGroupClickListener;
    private OnCreateGroupClickListener onCreateGroupClickListener;
    private String actionText;
    private @LayoutRes int layoutResId = R.layout.search_groups_item;
    private @DrawableRes int backgroundResId = R.drawable.clickable_light;
    private boolean isSmall;

    public SearchGroupsAdapter(PoolMember poolMember, OnGroupClickListener onGroupClickListener, OnCreateGroupClickListener onCreateGroupClickListener) {
        super(poolMember);
        this.onGroupClickListener = onGroupClickListener;
        this.onCreateGroupClickListener = onCreateGroupClickListener;
    }

    public SearchGroupsAdapter setLayoutResId(int layoutResId) {
        this.layoutResId = layoutResId;
        return this;
    }

    public SearchGroupsAdapter setBackgroundResId(int backgroundResId) {
        this.backgroundResId = backgroundResId;
        return this;
    }

    @NonNull
    @Override
    public SearchGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchGroupsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchGroupsViewHolder holder, int position) {
        if (position >= getItemCount() - getCreateGroupCount()) {
            holder.pool = tempPool();
            holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.create_group));
            holder.name.setText(createPublicGroupName);
            holder.about.setText($(ResourcesHandler.class).getResources().getString(R.string.add_new_public_group));
            holder.backgroundPhoto.setVisibility(View.GONE);
            holder.actionRecyclerView.setVisibility(View.GONE);
            holder.cardView.setOnClickListener(view -> {
                if (onCreateGroupClickListener != null) {
                    onCreateGroupClickListener.onCreateGroupClicked(createPublicGroupName);
                }
            });
            holder.cardView.setOnLongClickListener(null);
            holder.cardView.setBackgroundResource(isSmall ? backgroundResId : R.drawable.clickable_green_4dp);
            return;
        }

        Group group = groups.get(position);

        holder.name.setText($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
        if (!group.hasEvent() && !group.isPublic()) {
            holder.cardView.setBackgroundResource(isSmall ? backgroundResId : R.drawable.clickable_blue_4dp);
            holder.action.setText(actionText != null ? actionText : $(ResourcesHandler.class).getResources().getString(R.string.open_group));
            holder.about.setText($(ResourcesHandler.class).getResources().getString(R.string.private_group));
        } else if (group.isPhysical()) {
            holder.cardView.setBackgroundResource(isSmall ? backgroundResId : R.drawable.clickable_purple_4dp);
            holder.action.setText(actionText != null ? actionText : $(ResourcesHandler.class).getResources().getString(R.string.open_group));
            holder.about.setText($(Val.class).of(group.getAbout()));
        } else if (group.hasEvent()) {
            holder.cardView.setBackgroundResource(isSmall ? backgroundResId : R.drawable.clickable_red_4dp);
            holder.action.setText(actionText != null ? actionText : $(ResourcesHandler.class).getResources().getString(R.string.open_event));
            Event event = $(StoreHandler.class).getStore().box(Event.class).query()
                    .equal(Event_.id, group.getEventId())
                    .build().findFirst();
            holder.about.setText(event != null ? $(EventDetailsHandler.class).formatEventDetails(event) :
                $(ResourcesHandler.class).getResources().getString(R.string.event));
        } else {
            holder.cardView.setBackgroundResource(isSmall ? backgroundResId : R.drawable.clickable_green_4dp);
            holder.action.setText(actionText != null ? actionText : $(ResourcesHandler.class).getResources().getString(R.string.open_group));
            holder.about.setText($(Val.class).of(group.getAbout()));
        }
        holder.cardView.setOnClickListener(view -> {
            if (onGroupClickListener != null) {
                onGroupClickListener.onGroupClicked(group, holder.itemView);
            }
        });
        holder.cardView.setOnLongClickListener(view -> {
            $(GroupMemberHandler.class).changeGroupSettings(group);
            return true;
        });

        holder.pool = tempPool();

        if (isSmall) {
            holder.actionRecyclerView.setVisibility(View.GONE);
        } else {
            holder.actionRecyclerView.setVisibility(View.VISIBLE);
            holder.pool.$(ApplicationHandler.class).setApp($(ApplicationHandler.class).getApp());
            holder.pool.$(ActivityHandler.class).setActivity($(ActivityHandler.class).getActivity());
            holder.pool.$(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());
            holder.pool.$(GroupActionRecyclerViewHandler.class).attach(holder.actionRecyclerView, GroupActionAdapter.Layout.TEXT);
            holder.pool.$(GroupActionRecyclerViewHandler.class).setOnGroupActionRepliedListener(groupAction -> $(SearchHandler.class).openGroup(groupAction.getGroup(), holder.itemView));
            holder.pool.$(DisposableHandler.class).add($(StoreHandler.class).getStore().box(GroupAction.class).query()
                    .equal(GroupAction_.group, group.getId())
                    .build().subscribe().single()
                    .on(AndroidScheduler.mainThread())
                    .observer(groupActions -> {
                        holder.pool.$(GroupActionRecyclerViewHandler.class).getRecyclerView().setVisibility(groupActions.isEmpty() ? View.GONE : View.VISIBLE);
                        holder.pool.$(GroupActionRecyclerViewHandler.class).getAdapter().setGroupActions(groupActions);
                    }));

            $(ImageHandler.class).get().cancelRequest(holder.backgroundPhoto);
            if (group.getPhoto() != null) {
                holder.backgroundPhoto.setVisibility(View.VISIBLE);
                holder.backgroundPhoto.setImageDrawable(null);
                $(ImageHandler.class).get().load(group.getPhoto() + "?s=32")
                        .noPlaceholder()
                        .transform(new BlurTransformation($(ActivityHandler.class).getActivity(), 2))
                        .into(holder.backgroundPhoto);
            } else {
                holder.backgroundPhoto.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onViewRecycled(@NonNull SearchGroupsViewHolder holder) {
        holder.pool.end();
    }

    @Override
    public int getItemCount() {
        return groups.size() + getCreateGroupCount();
    }

    public void setCreatePublicGroupName(String createPublicGroupName) {
        this.createPublicGroupName = createPublicGroupName;
        notifyDataSetChanged();
    }

    public void setGroups(List<Group> groups) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return SearchGroupsAdapter.this.groups.size();
            }

            @Override
            public int getNewListSize() {
                return groups.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                return SearchGroupsAdapter.this.groups.get(oldPosition).getId() != null &&
                        groups.get(newPosition).getId() != null &&
                        SearchGroupsAdapter.this.groups.get(oldPosition).getId().equals(groups.get(newPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                return SearchGroupsAdapter.this.groups.get(oldPosition).getName() != null &&
                        groups.get(newPosition).getName() != null &&
                        SearchGroupsAdapter.this.groups.get(oldPosition).getName().equals(groups.get(newPosition).getName());
            }
        });
        this.groups.clear();
        this.groups.addAll(groups);
        diffResult.dispatchUpdatesTo(this);
    }

    private int getCreateGroupCount() {
        return (createPublicGroupName == null ? 0 : 1);
    }

    public void setActionText(String actionText) {
        this.actionText = actionText;
    }

    public void setIsSmall(boolean isSmall) {
        this.isSmall = isSmall;
    }

    protected class SearchGroupsViewHolder extends RecyclerView.ViewHolder {
        View cardView;
        TextView name;
        TextView about;
        TextView action;
        RecyclerView actionRecyclerView;
        TempPool pool;
        ImageView backgroundPhoto;

        public SearchGroupsViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.rootView);
            name = itemView.findViewById(R.id.name);
            about = itemView.findViewById(R.id.about);
            action = itemView.findViewById(R.id.action);
            actionRecyclerView = itemView.findViewById(R.id.actionRecyclerView);
            backgroundPhoto = itemView.findViewById(R.id.backgroundPhoto);
        }
    }

    public interface OnGroupClickListener {
        void onGroupClicked(Group group, View view);
    }

    public interface OnCreateGroupClickListener {
        void onCreateGroupClicked(String groupName);
    }
}
