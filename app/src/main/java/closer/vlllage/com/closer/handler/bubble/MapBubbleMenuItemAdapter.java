package closer.vlllage.com.closer.handler.bubble;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class MapBubbleMenuItemAdapter extends PoolRecyclerAdapter<MapBubbleMenuItemAdapter.MenuItemViewHolder> {

    private List<MapBubbleMenuItem> menuItems = new ArrayList<>();
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
        MapBubbleMenuItem menuItem = menuItems.get(position);
        holder.menuItemTitle.setText(menuItem.getTitle());

        holder.menuItemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(menuItem.getIconRes(), 0, 0, 0);

        holder.itemView.setOnClickListener(view -> onClickListener.onMenuItemClick(mapBubble, position));
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public void setMenuItems(List<MapBubbleMenuItem> menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(menuItems);
        notifyDataSetChanged();
    }

    public void setMenuItems(MapBubbleMenuItem... menuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(Arrays.asList(menuItems));
        notifyDataSetChanged();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {

        TextView menuItemTitle;

        public MenuItemViewHolder(View itemView) {
            super(itemView);
            menuItemTitle = itemView.findViewById(R.id.menuItemTitle);
        }
    }
}
