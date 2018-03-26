package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.AlertHandler;
import closer.vlllage.com.closer.handler.VerifyNumberHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class MyGroupsAdapter extends PoolRecyclerAdapter<MyGroupsAdapter.MyGroupViewHolder> {

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

        groupName.setBackgroundResource(position == 0 ? R.drawable.clickable_accent : position == getItemCount() - 1 ? R.drawable.clickable_blue_light : R.drawable.clickable_blue);
        groupName.setText(position == 0 ? "Verify your number" : position == getItemCount() - 1 ? "+ New Group" : "888 Friends");

        if (position == 0) {
            groupName.setOnClickListener(view -> {
                $(VerifyNumberHandler.class).verify();
            });
        } else if (position == getItemCount() - 1) {
            groupName.setOnClickListener(view -> {
                $(AlertHandler.class).showAlert(R.layout.set_name_modal, R.string.create_group, R.string.create_group, null, null);
            });
        } else {
            groupName.setOnClickListener(view -> {
                $(GroupActivityTransitionHandler.class).showGroupMessages(holder.itemView, "1");
            });
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    class MyGroupViewHolder extends RecyclerView.ViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }
}
