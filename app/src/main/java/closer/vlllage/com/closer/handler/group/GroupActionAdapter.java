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
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.GroupAction;


public class GroupActionAdapter extends PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder> {

    private List<GroupAction> actions = new ArrayList<>();

    public GroupActionAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public GroupActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupActionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_action_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupActionViewHolder holder, int position) {
        holder.actionName.setText(actions.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public void setActions(List<GroupAction> actions) {
        this.actions = actions;
    }

    class GroupActionViewHolder extends RecyclerView.ViewHolder {

        TextView actionName;

        public GroupActionViewHolder(View itemView) {
            super(itemView);
            actionName = itemView.findViewById(R.id.actionName);
        }
    }
}
