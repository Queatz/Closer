package closer.vlllage.com.closer.handler.share;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.search.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.RecyclerViewHeader;

public class SearchGroupsHeaderAdapter extends SearchGroupsAdapter {

    private RecyclerViewHeader header = new RecyclerViewHeader();

    public SearchGroupsHeaderAdapter(PoolMember poolMember, OnGroupClickListener onGroupClickListener, OnCreateGroupClickListener onCreateGroupClickListener) {
        super(poolMember, onGroupClickListener, onCreateGroupClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchGroupsAdapter.SearchGroupsViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        header.onBind(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull SearchGroupsAdapter.SearchGroupsViewHolder holder) {
        super.onViewRecycled(holder);
        header.onRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        header.attach(recyclerView, $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeakHeight) * 2);
    }
}
