package closer.vlllage.com.closer.handler.bubble;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class MapBubbleMenuItemAdapter extends PoolRecyclerAdapter<MapBubbleMenuItemAdapter.MenuItemViewHolder> {

    private MapBubbleMenuView.OnMapBubbleMenuItemClickListener onClickListener;
    private MapBubble mapBubble;

    public MapBubbleMenuItemAdapter(PoolMember poolMember, MapBubble mapBubble, MapBubbleMenuView.OnMapBubbleMenuItemClickListener onClickListener) {
        super(poolMember);
        this.onClickListener = onClickListener;
        this.mapBubble = mapBubble;
    }

    @NonNull
    @Override
    public MapBubbleMenuItemAdapter.MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MapBubbleMenuItemAdapter.MenuItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_bubble_menu_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MapBubbleMenuItemAdapter.MenuItemViewHolder holder, int position) {
        if (position == 0) {
            holder.menuItemTitle.setText("Add a suggestions...");
        } else {
            holder.menuItemTitle.setText("Manage my suggestions...");
        }

        holder.itemView.setOnClickListener(view -> onClickListener.onMenuItemClick(mapBubble, position));
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {

        TextView menuItemTitle;

        public MenuItemViewHolder(View itemView) {
            super(itemView);
            menuItemTitle = itemView.findViewById(R.id.menuItemTitle);
        }
    }
}
