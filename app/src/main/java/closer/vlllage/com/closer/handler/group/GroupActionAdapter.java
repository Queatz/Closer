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

    private List<GroupAction> groupActions = new ArrayList<>();

    private OnGroupActionClickListener onGroupActionClickListener;
    private OnGroupActionLongClickListener onGroupActionLongClickListener;

    public GroupActionAdapter(PoolMember poolMember,
                              OnGroupActionClickListener onGroupActionClickListener,
                              OnGroupActionLongClickListener onGroupActionLongClickListener) {
        super(poolMember);
        this.onGroupActionClickListener = onGroupActionClickListener;
        this.onGroupActionLongClickListener = onGroupActionLongClickListener;
    }

    @NonNull
    @Override
    public GroupActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupActionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_action_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupActionViewHolder holder, int position) {
        GroupAction groupAction = groupActions.get(position);
        holder.actionName.setText(groupActions.get(position).getName());
        holder.actionName.setOnClickListener(view -> {
            if (onGroupActionClickListener != null) {
                onGroupActionClickListener.onGroupActionClick(groupAction);
            }
        });

        holder.actionName.setOnLongClickListener(view -> {
            if (onGroupActionLongClickListener != null) {
                onGroupActionLongClickListener.onGroupActionLongClick(groupAction);
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return groupActions.size();
    }

    public void setGroupActions(List<GroupAction> groupActions) {
        this.groupActions = groupActions;
        notifyDataSetChanged();
    }

    class GroupActionViewHolder extends RecyclerView.ViewHolder {

        TextView actionName;

        public GroupActionViewHolder(View itemView) {
            super(itemView);
            actionName = itemView.findViewById(R.id.actionName);
        }
    }

    public interface OnGroupActionClickListener {
        void onGroupActionClick(GroupAction groupAction);
    }

    public interface OnGroupActionLongClickListener {
        void onGroupActionLongClick(GroupAction groupAction);
    }
}
