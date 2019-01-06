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
import closer.vlllage.com.closer.handler.helpers.PhoneListActivityTransitionHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.ReactionCount;

public class ReactionAdapter extends PoolRecyclerAdapter<ReactionAdapter.ViewHolder> {

    private List<ReactionCount> items = new ArrayList<>();
    private GroupMessage groupMessage;

    public ReactionAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.reaction_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ReactionCount reaction = items.get(position);

        viewHolder.reaction.setText(reaction.reaction + " " + reaction.count);
        viewHolder.reaction.setOnClickListener(v -> {
            $(PhoneListActivityTransitionHandler.class).showReactions(groupMessage.getId());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ReactionAdapter setItems(List<ReactionCount> items) {
        this.items = items;
        notifyDataSetChanged();
        return this;
    }

    public void setGroupMessage(GroupMessage groupMessage) {
        this.groupMessage = groupMessage;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        TextView reaction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reaction = (TextView) itemView;
        }
    }
}
