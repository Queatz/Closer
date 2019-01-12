package closer.vlllage.com.closer.handler.phone;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.ReactionResult;
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.PhotoHelper;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class PhoneAdapter extends PoolRecyclerAdapter<PhoneAdapter.ViewHolder> {

    private final OnReactionClickListener onReactionClickListener;
    private List<ReactionResult> items = new ArrayList<>();

    public PhoneAdapter(PoolMember poolMember, OnReactionClickListener onReactionClickListener) {
        super(poolMember);
        this.onReactionClickListener = onReactionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.phone_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ReactionResult reaction = items.get(position);

        viewHolder.name.setText(reaction.phone.name);
        viewHolder.reaction.setText(reaction.reaction);

        if ($(Val.class).isEmpty(reaction.phone.photo)) {
            viewHolder.photo.setVisibility(View.GONE);
        } else {
            viewHolder.photo.setVisibility(View.VISIBLE);
            $(PhotoHelper.class).loadCircle(viewHolder.photo, reaction.phone.photo);
            viewHolder.photo.setOnClickListener(v -> {
                $(PhotoActivityTransitionHandler.class).show(viewHolder.photo, reaction.phone.photo);
            });
        }

        viewHolder.itemView.setOnClickListener(v -> {
            onReactionClickListener.onReactionClick(reaction);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<ReactionResult> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public List<ReactionResult> getItems() {
        return items;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView photo;
        TextView name;
        TextView reaction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.photo);
            name = itemView.findViewById(R.id.name);
            reaction = itemView.findViewById(R.id.reaction);
        }
    }

    public interface OnReactionClickListener {
        void onReactionClick(ReactionResult  reaction);
    }
}
