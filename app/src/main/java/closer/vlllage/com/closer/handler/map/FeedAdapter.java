package closer.vlllage.com.closer.handler.map;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.GroupMessagesAdapter;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class FeedAdapter extends GroupMessagesAdapter {

    private GroupMessagesAdapter.GroupMessageViewHolder headerViewHolder;
    private HeaderHeightCallback headerHeightCallback;

    public FeedAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessagesAdapter.GroupMessageViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
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
    public void onViewRecycled(@NonNull GroupMessagesAdapter.GroupMessageViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder == headerViewHolder) {
            headerViewHolder = null;
            ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams());
            params.topMargin = 0;
            holder.itemView.setLayoutParams(params);
        }
    }

    public void notifyHeaderHeightChanged() {
        setHeaderMargin();
    }

    public void setHeaderHeightCallback(HeaderHeightCallback headerHeightCallback) {
        this.headerHeightCallback = headerHeightCallback;
    }

    public interface HeaderHeightCallback {
        int getHeaderHeight();
    }
}
