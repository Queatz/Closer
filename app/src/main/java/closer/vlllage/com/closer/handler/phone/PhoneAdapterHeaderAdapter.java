package closer.vlllage.com.closer.handler.phone;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.RecyclerViewHeader;

public class PhoneAdapterHeaderAdapter extends PhoneAdapter {

    private RecyclerViewHeader header = new RecyclerViewHeader();

    public PhoneAdapterHeaderAdapter(PoolMember poolMember, OnReactionClickListener onReactionClickListener) {
        super(poolMember, onReactionClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        header.onBind(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull PhoneAdapter.ViewHolder holder) {
        super.onViewRecycled(holder);
        header.onRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        header.attach(recyclerView, $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeakHeight) * 2);
    }
}
