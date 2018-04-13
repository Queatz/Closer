package closer.vlllage.com.closer.handler.search;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.handler.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.Group;

public class SearchGroupsAdapter extends PoolRecyclerAdapter<SearchGroupsAdapter.SearchGroupsViewHolder> {

    private String createPublicGroupName = null;
    private final List<Group> groups = new ArrayList<>();
    private OnGroupClickListener onGroupClickListener;
    private OnCreateGroupClickListener onCreateGroupClickListener;

    public SearchGroupsAdapter(PoolMember poolMember, OnGroupClickListener onGroupClickListener, OnCreateGroupClickListener onCreateGroupClickListener) {
        super(poolMember);
        this.onGroupClickListener = onGroupClickListener;
        this.onCreateGroupClickListener = onCreateGroupClickListener;
    }

    @NonNull
    @Override
    public SearchGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchGroupsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_groups_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchGroupsViewHolder holder, int position) {
        if (position < getCreateGroupCount()) {
            holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.create_group));
            holder.name.setText(createPublicGroupName);
            holder.about.setText($(ResourcesHandler.class).getResources().getString(R.string.add_new_group));
            holder.itemView.setOnClickListener(view -> {
                if (onCreateGroupClickListener != null) {
                    onCreateGroupClickListener.onCreateGroupClicked(createPublicGroupName);
                }
            });
            return;
        }

        Group group = groups.get(position - getCreateGroupCount());

        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.open));
        holder.name.setText(group.getName());
        holder.about.setText($(Val.class).of(group.getAbout()));
        holder.itemView.setOnClickListener(view -> {
            if (onGroupClickListener != null) {
                onGroupClickListener.onGroupClicked(group);
            }
        });
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
        this.groups.clear();
        this.groups.addAll(groups);
        notifyDataSetChanged();
    }

    private int getCreateGroupCount() {
        return (createPublicGroupName == null ? 0 : 1);
    }

    class SearchGroupsViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView about;
        TextView action;

        public SearchGroupsViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            about = itemView.findViewById(R.id.about);
            action = itemView.findViewById(R.id.action);
        }
    }

    public interface OnGroupClickListener {
        void onGroupClicked(Group group);
    }

    public interface OnCreateGroupClickListener {
        void onCreateGroupClicked(String groupName);
    }
}
