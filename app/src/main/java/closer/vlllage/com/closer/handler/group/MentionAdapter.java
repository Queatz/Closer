package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.StringHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.Phone;

public class MentionAdapter extends PoolRecyclerAdapter<MentionAdapter.ViewHolder> {

    private final List<Phone> items = new ArrayList<>();
    private OnMentionClickListener onMentionClickListener;

    public MentionAdapter(PoolMember poolMember, OnMentionClickListener onMentionClickListener) {
        super(poolMember);
        this.onMentionClickListener = onMentionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_action_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Phone phone = items.get(position);
        holder.mentionName.setText(items.get(position).getName());

        holder.mentionName.setOnClickListener(view -> {
            if (onMentionClickListener != null) {
                onMentionClickListener.onMentionClick(phone);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Phone> items) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return MentionAdapter.this.items.size();
            }

            @Override
            public int getNewListSize() {
                return items.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                return $(StringHandler.class).equals(MentionAdapter.this.items.get(oldPosition).getId(), items.get(newPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                return $(StringHandler.class).equals(MentionAdapter.this.items.get(oldPosition).getName(), items.get(newPosition).getName());
            }
        }, true);
        this.items.clear();
        this.items.addAll(items);
        diffResult.dispatchUpdatesTo(this);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mentionName;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setClipToOutline(true);
            mentionName = itemView.findViewById(R.id.actionName);
        }
    }

    public interface OnMentionClickListener {
        void onMentionClick(Phone phone);
    }
}
