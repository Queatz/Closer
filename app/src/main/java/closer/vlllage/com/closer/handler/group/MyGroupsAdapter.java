package closer.vlllage.com.closer.handler.group;

import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.GroupActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ActivityHandler;
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

        groupName.setBackgroundResource(position == getItemCount() - 1 ? R.drawable.clickable_blue_light : R.drawable.clickable_blue);
        groupName.setText(position == getItemCount() - 1 ? "+ New Group" : "888 Friends");
        groupName.setOnClickListener(view -> {
            Intent intent = new Intent($(ActivityHandler.class).getActivity(), GroupActivity.class);
            Rect bounds = new Rect();
            holder.itemView.getGlobalVisibleRect(bounds);

            // Offset for status bar
            final int[] location = new int[2];
            holder.itemView.getRootView().findViewById(android.R.id.content).getLocationInWindow(location);
            int windowTopOffset = location[1];
            bounds.offset(0, -windowTopOffset);

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.setSourceBounds(bounds);
            $(ActivityHandler.class).getActivity().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    class MyGroupViewHolder extends RecyclerView.ViewHolder {
        public MyGroupViewHolder(View itemView) {
            super(itemView);
        }
    }
}
