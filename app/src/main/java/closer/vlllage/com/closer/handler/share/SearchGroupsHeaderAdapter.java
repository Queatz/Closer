package closer.vlllage.com.closer.handler.share;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.ui.RecyclerViewHeader;

public class SearchGroupsHeaderAdapter extends SearchGroupsAdapter {

    private static final int HEADER_VIEW_TYPE = -1;
    private final OnQueryChangedListener onQueryChangedListener;

    private RecyclerViewHeader header = new RecyclerViewHeader();

    private String headerText;

    public SearchGroupsHeaderAdapter(PoolMember poolMember, OnGroupClickListener onGroupClickListener, OnCreateGroupClickListener onCreateGroupClickListener, OnQueryChangedListener onQueryChangedListener) {
        super(poolMember, onGroupClickListener, onCreateGroupClickListener);
        this.onQueryChangedListener = onQueryChangedListener;
        setNoAnimation(true);
    }

    @NonNull
    @Override
    public SearchGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER_VIEW_TYPE:
                return new SearchGroupsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_text_header_item, parent, false));
        }

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchGroupsAdapter.SearchGroupsViewHolder holder, int position) {
        if (position == 0) {
            holder.name.setText(headerText);
            EditText searchGroups = holder.itemView.findViewById(R.id.searchGroups);

            searchGroups.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    onQueryChangedListener.onQueryChanged(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            super.onBindViewHolder(holder, position - 1);
        }

        header.onBind(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull SearchGroupsViewHolder holder) {
        super.onViewRecycled(holder);
        header.onRecycled(holder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        header.attach(recyclerView, $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeekHeight) * 2);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? HEADER_VIEW_TYPE : super.getItemViewType(position - 1);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    public SearchGroupsHeaderAdapter setHeaderText(String headerText) {
        this.headerText = headerText;
        return this;
    }

    public interface OnQueryChangedListener {
        void onQueryChanged(String query);
    }
}
