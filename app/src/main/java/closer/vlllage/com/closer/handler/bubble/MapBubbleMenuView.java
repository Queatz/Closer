package closer.vlllage.com.closer.handler.bubble;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {

    }

    public interface OnMapBubbleMenuItemClickListener {
        void onMenuItemClick(MapBubble mapBubble, int position);
    }
}
