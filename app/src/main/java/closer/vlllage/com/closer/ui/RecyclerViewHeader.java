package closer.vlllage.com.closer.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerViewHeader {

    private RecyclerView.ViewHolder headerViewHolder;
    private RecyclerView.ViewHolder footerViewHolder;
    private RecyclerView recyclerView;
    private int pad;

    private int originalHeaderPadding;
    private int originalFooterPadding;

    public void onBind(RecyclerView.ViewHolder holder, int position) {
        if (position == recyclerView.getAdapter().getItemCount() - 1) {
            originalFooterPadding = holder.itemView.getPaddingBottom();

            holder.itemView.setPaddingRelative(
                    holder.itemView.getPaddingStart(),
                    holder.itemView.getPaddingTop(),
                    holder.itemView.getPaddingEnd(),
                    holder.itemView.getPaddingBottom() * 2
            );

            footerViewHolder = holder;
            recyclerView.post(this::setHeaderMargin);
        }

        if (position == 0) {
            originalHeaderPadding = holder.itemView.getPaddingTop();

            holder.itemView.setPaddingRelative(
                    holder.itemView.getPaddingStart(),
                    holder.itemView.getPaddingTop() * 2,
                    holder.itemView.getPaddingEnd(),
                    holder.itemView.getPaddingBottom()
            );

            headerViewHolder = holder;
            setHeaderMargin();
        }
    }

    public void onRecycled(RecyclerView.ViewHolder holder) {
        if (holder == headerViewHolder) {
            holder.itemView.setPaddingRelative(
                    holder.itemView.getPaddingStart(),
                    originalHeaderPadding,
                    holder.itemView.getPaddingEnd(),
                    holder.itemView.getPaddingBottom()
            );

            headerViewHolder = null;
            ViewGroup.MarginLayoutParams params = ((ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams());
            params.topMargin = 0;
            holder.itemView.setLayoutParams(params);
        }

        if (holder == footerViewHolder) {
            holder.itemView.setPaddingRelative(
                    holder.itemView.getPaddingStart(),
                    holder.itemView.getPaddingTop(),
                    holder.itemView.getPaddingEnd(),
                    originalFooterPadding
            );

            footerViewHolder = null;
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
        params.topMargin = (recyclerView == null ? 0 : recyclerView.getHeight() - pad + extend());
        headerViewHolder.itemView.setLayoutParams(params);
    }

    private int extend() {
        if (footerViewHolder == null) {
            return 0;
        }

        if (footerViewHolder.itemView.getBottom() == 0) {
            return 0;
        }

        return Math.max(0, recyclerView.getHeight() - footerViewHolder.itemView.getBottom());
    }
}
