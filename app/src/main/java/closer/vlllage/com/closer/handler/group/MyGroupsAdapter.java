package closer.vlllage.com.closer.handler.group;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.Group;

public class MyGroupsAdapter extends PoolRecyclerAdapter<MyGroupsAdapter.MyGroupViewHolder> {

    private List<GroupActionBarButton> actions = new ArrayList<>();
    private List<GroupActionBarButton> endActions = new ArrayList<>();
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
            GroupActionBarButton actionBarButton = actions.get(position);
            groupName.setBackgroundResource(actionBarButton.getBackgroundDrawableRes());
            groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(actionBarButton.getIcon(), 0, 0, 0);
            groupName.setCompoundDrawablePadding($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupName.setCompoundDrawableTintList(ColorStateList.valueOf(
                        $(ResourcesHandler.class).getResources().getColor(android.R.color.white, $(ActivityHandler.class).getActivity().getTheme())
                ));
            }
            groupName.setText(actionBarButton.getName());
            groupName.setOnClickListener(actionBarButton.getOnClick());
            groupName.setOnLongClickListener(view -> {
                if (actionBarButton.getOnLongClick() != null) {
                    actionBarButton.getOnLongClick().onClick(view);
                    return true;
                }

                return false;
            });
            return;
        }

        position -= actions.size();
        boolean isEndActionButton = position >= groups.size();

        if (isEndActionButton) {
            GroupActionBarButton actionBarButton = endActions.get(position - groups.size());
            groupName.setBackgroundResource(actionBarButton.getBackgroundDrawableRes());
            groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(actionBarButton.getIcon(), 0, 0, 0);
            groupName.setCompoundDrawablePadding($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupName.setCompoundDrawableTintList(ColorStateList.valueOf(
                        $(ResourcesHandler.class).getResources().getColor(android.R.color.white, $(ActivityHandler.class).getActivity().getTheme())
                ));
            }
            groupName.setText(actionBarButton.getName());
            groupName.setOnClickListener(actionBarButton.getOnClick());
            groupName.setOnLongClickListener(view -> {
                if (actionBarButton.getOnLongClick() != null) {
                    actionBarButton.getOnLongClick().onClick(view);
                    return true;
                }

                return false;
            });
            return;
        }

        Group group = groups.get(position);
        groupName.setBackgroundResource(R.drawable.clickable_blue);
        groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
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
                $(DefaultAlerts.class).message($(ResourcesHandler.class).getResources().getString(R.string.group_no_more, group.getName()));
                $(RefreshHandler.class).refreshMyGroups();
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));
    }

    @Override
    public int getItemCount() {
        return actions.size() + groups.size() + endActions.size();
    }

    public void setGroups(@NonNull List<Group> groups) {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void setActions(@NonNull List<GroupActionBarButton> actions) {
        this.actions = actions;
        notifyDataSetChanged();
    }

    public MyGroupsAdapter setEndActions(List<GroupActionBarButton> endActions) {
        this.endActions = endActions;
        notifyDataSetChanged();
        return this;
    }

    class MyGroupViewHolder extends RecyclerView.ViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }
}
