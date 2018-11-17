package closer.vlllage.com.closer.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.feed.PublicGroupFeedItemHandler;
import closer.vlllage.com.closer.handler.group.GroupMemberHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;

import static closer.vlllage.com.closer.pool.Pool.tempPool;

public class FeedInjectionsAdapter extends PoolRecyclerAdapter<FeedInjectionsAdapter.ViewHolder> implements CombinedRecyclerAdapter.PrioritizedAdapter {

    public FeedInjectionsAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResId;

        switch (viewType) {
            case 0:
                layoutResId = R.layout.feed_item_public_groups;
                break;
            case 1:
                layoutResId = R.layout.feed_item_card;
                break;
            default:
                throw new IllegalStateException("Unimplemented feed injection item type");
        }

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.pool = tempPool();

        switch (getItemViewType(position)) {
            case 0:
                holder.pool.$set($(StoreHandler.class));
                holder.pool.$set($(SyncHandler.class));
                holder.pool.$set($(MapHandler.class));
                holder.pool.$set($(ApplicationHandler.class));
                holder.pool.$set($(ActivityHandler.class));
                holder.pool.$set($(SortHandler.class));
                holder.pool.$set($(KeyboardHandler.class));
                holder.pool.$set($(GroupMemberHandler.class));
                holder.pool.$(PublicGroupFeedItemHandler.class).attach(holder.itemView);
                break;
            case 1:

                break;
            default:
                throw new IllegalStateException("Unimplemented feed injection item type");
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.pool.end();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public int getItemPriority(int position) {
        return position * 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TempPool pool;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
