package closer.vlllage.com.closer.handler.map;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.ui.RecyclerViewHeader;

public abstract class HeaderAdapter<T extends RecyclerView.ViewHolder> extends PoolRecyclerAdapter<T> {

    private RecyclerViewHeader header = new RecyclerViewHeader();

    public HeaderAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        header.onBind(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull T holder) {
        header.onRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        header.attach(recyclerView, $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeakHeight));
    }
}
