package closer.vlllage.com.closer.handler.bubble;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubbleMenuView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleMenuView.OnMapBubbleMenuItemClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_menu, layer, false);

        RecyclerView recyclerView = view.findViewById(R.id.menuRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(
                layer.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));

        recyclerView.setAdapter(new MapBubbleMenuItemAdapter(this, mapBubble, onClickListener));

        return view;
    }

    public MapBubbleMenuItemAdapter getMenuAdapter(MapBubble mapBubble) {
        return (MapBubbleMenuItemAdapter) ((RecyclerView) mapBubble.getView().findViewById(R.id.menuRecyclerView)).getAdapter();
    }

    public void setMenuTitle(MapBubble mapBubble, String title) {
        TextView menuTitle = mapBubble.getView().findViewById(R.id.menuTitle);
        if (title == null || title.isEmpty()) {
            menuTitle.setVisibility(View.GONE);
        } else {
            menuTitle.setVisibility(View.VISIBLE);
            menuTitle.setText(title);
        }
    }

    public interface OnMapBubbleMenuItemClickListener {
        void onMenuItemClick(MapBubble mapBubble, int position);
    }
}
