package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.GroupMessage;

public class GroupMessagesAdapter extends PoolRecyclerAdapter<GroupMessagesAdapter.GroupMessageViewHolder> {

    private List<GroupMessage> messages = new ArrayList<>();
    private OnMessageClickListener onMessageClickListener;

    public GroupMessagesAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListener = onMessageClickListener;
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupMessageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        holder.itemView.setOnClickListener(view -> {
            if (onMessageClickListener != null) {
                onMessageClickListener.onMessageClick(messages.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        public GroupMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface OnMessageClickListener {
        void onMessageClick(GroupMessage message);
    }
}
