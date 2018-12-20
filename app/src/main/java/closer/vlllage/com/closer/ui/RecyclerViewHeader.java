package closer.vlllage.com.closer.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerViewHeader {

    private RecyclerView.ViewHolder headerViewHolder;
    private RecyclerView recyclerView;
    private int pad;



    public void onBind(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            headerViewHolder = holder;
            setHeaderMargin();
        }
    }

    public void onRecycled(RecyclerView.ViewHolder holder) {
        if (holder == headerViewHolder) {
            headerViewHolder = null;
            ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams());
            params.topMargin = 0;
            holder.itemView.setLayoutParams(params);
        }
    }

    public void attach(RecyclerView recyclerView, int pad) {
        this.pad = pad;
        this.recyclerView = recyclerView;
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (top != oldTop || bottom != oldBottom) {
                setHeaderMargin();
            }
        });
    }

    private void setHeaderMargin() {
        if (headerViewHolder == null) {
            return;
        }

        ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) headerViewHolder.itemView.getLayoutParams());
        params.topMargin = (recyclerView == null ? 0 : recyclerView.getHeight() - pad);
        headerViewHolder.itemView.setLayoutParams(params);
    }
}
