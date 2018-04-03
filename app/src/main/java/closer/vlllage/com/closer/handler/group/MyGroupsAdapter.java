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
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.handler.SyncHandler;
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

        if (position < actions.size()) {
            groupName.setBackgroundResource(R.drawable.clickable_accent);
            groupName.setText(actions.get(position));
            groupName.setOnClickListener(view -> $(VerifyNumberHandler.class).verify());
            return;
        }

        position -= actions.size();
        boolean isNewGroupButton = position >= groups.size();

        if (isNewGroupButton) {
            groupName.setBackgroundResource(R.drawable.clickable_blue_light);
            groupName.setText($(ResourcesHandler.class).getResources().getString(R.string.add_new_group));
            groupName.setOnClickListener(view ->
                    $(AlertHandler.class).make()
                            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.create_group))
                            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.create_a_new_group))
                            .setLayoutResId(R.layout.create_group_modal)
                            .setTextView(R.id.input, name -> createGroup(groupName, name))
                            .show());
            return;
        }

        Group group = groups.get(position);
        groupName.setBackgroundResource(R.drawable.clickable_blue);
        groupName.setText(group.getName());

        groupName.setOnClickListener(view ->
                $(GroupActivityTransitionHandler.class).showGroupMessages(holder.itemView, group.getId()));

        groupName.setOnLongClickListener(view -> {
            $(AlertHandler.class).make()
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.leave_group, group.getName()))
                    .setPositiveButtonCallback(result -> leaveGroup(group))
                    .setTitle($(ResourcesHandler.class).getResources().getString(R.string.leave_group_title, group.getName()))
                    .setMessage($(ResourcesHandler.class).getResources().getString(R.string.leave_group_message))
                    .show();

            return true;
        });
    }

    private void leaveGroup(Group group) {
        $(DisposableHandler.class).add($(ApiHandler.class).leaveGroup(group.getId()).subscribe(successResult -> {
            if (successResult.success) {
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.group_no_more, group.getName()))
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.ok))
                        .show();
                $(RefreshHandler.class).refreshMyGroups();
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    private void createGroup(View view, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }

        Group group = $(StoreHandler.class).create(Group.class);
        group.setName(name);
        $(StoreHandler.class).getStore().box(Group.class).put(group);
        $(SyncHandler.class).sync(group);

        $(GroupActivityTransitionHandler.class).showGroupMessages(view, group.getId());
    }

    @Override
    public int getItemCount() {
        return actions.size() + groups.size() + 1/* New Group */;
    }

    public void setGroups(@NonNull List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void setActions(@NonNull List<String> actions) {
        this.actions = actions;
        notifyDataSetChanged();
    }

    class MyGroupViewHolder extends RecyclerView.ViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }
}
