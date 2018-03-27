package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.handler.VerifyNumberHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;

public class MyGroupsAdapter extends PoolRecyclerAdapter<MyGroupsAdapter.MyGroupViewHolder> {

    private List<String> actions = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();

    public MyGroupsAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public MyGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyGroupViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.group_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupViewHolder holder, int position) {
        TextView groupName = holder.itemView.findViewById(R.id.groupName);

        groupName.setBackgroundResource(position == 0 ? R.drawable.clickable_accent :
                position == getItemCount() - 1 ? R.drawable.clickable_blue_light :
                        R.drawable.clickable_blue);

        groupName.setText(position == 0 ? $(ResourcesHandler.class).getResources().getString(R.string.verify_your_number) :
                position == getItemCount() - 1 ? $(ResourcesHandler.class).getResources().getString(R.string.add_new_group) :
                groups.get(position - 1).getName());

        if (position == 0) {
            groupName.setOnClickListener(view -> $(VerifyNumberHandler.class).verify());
        } else if (position == getItemCount() - 1) {
            groupName.setOnClickListener(view ->
                    $(AlertHandler.class).makeAlert()
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_group))
                        .setTitle($(ResourcesHandler.class).getResources().getString(R.string.create_group))
                        .setLayoutResId(R.layout.set_name_modal)
                        .setTextView(R.id.input, this::createGroup)
                        .show());
        } else {
            groupName.setOnClickListener(view ->
                    $(GroupActivityTransitionHandler.class).showGroupMessages(holder.itemView, groups.get(position - 1).getId()));
        }
    }

    private void createGroup(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Group group = $(StoreHandler.class).create(Group.class);
        group.setName(name);
        $(StoreHandler.class).getStore().box(Group.class).put(group);
    }

    @Override
    public int getItemCount() {
        return 1 + 1 + groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    class MyGroupViewHolder extends RecyclerView.ViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }
}
