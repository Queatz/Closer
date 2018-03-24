package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;

public class MapBubbleMenuView {
    public static View from(ViewGroup layer, MapBubble mapBubble, MapBubbleMenuView.OnMapBubbleMenuItemClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_menu, layer, false);

        update(view, mapBubble);

        return view;
    }

    public static void update(View view, MapBubble mapBubble) {

    }

    public interface OnMapBubbleMenuItemClickListener {
        void onMenuItemClick(MapBubble mapBubble, int position);
    }
}
