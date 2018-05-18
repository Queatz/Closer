package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubblePhysicalGroupView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubblePhysicalGroupClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_physical_group, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onPhysicalGroupClick(mapBubble));
        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
    }

    public interface MapBubblePhysicalGroupClickListener {
        void onPhysicalGroupClick(MapBubble mapBubble);
    }
}
