package closer.vlllage.com.closer.handler.map;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class FeedAdapter extends PoolRecyclerAdapter<FeedAdapter.ViewHolder> {

    private ViewHolder headerViewHolder;
    private HeaderHeightCallback headerHeightCallback;

    public FeedAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FeedAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            headerViewHolder = holder;
            setHeaderMargin();
        }
    }

    private void setHeaderMargin() {
        if (headerViewHolder == null) {
            return;
        }
        int pad = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeakHeight);
        ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) headerViewHolder.itemView.getLayoutParams());
        params.topMargin = (headerHeightCallback == null ? 0 : headerHeightCallback.getHeaderHeight() - pad);
        headerViewHolder.itemView.setLayoutParams(params);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder == headerViewHolder) {
            headerViewHolder = null;
            ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams());
            params.topMargin = 0;
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return 25;
    }

    public void notifyHeaderHeightChanged() {
        setHeaderMargin();
    }

    public void setHeaderHeightCallback(HeaderHeightCallback headerHeightCallback) {
        this.headerHeightCallback = headerHeightCallback;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface HeaderHeightCallback {
        int getHeaderHeight();
    }
}
