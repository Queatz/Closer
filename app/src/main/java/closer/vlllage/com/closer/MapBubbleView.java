package closer.vlllage.com.closer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubbleView {
    public static View from(ViewGroup layer, MapBubble mapBubble, OnMapBubbleClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onMapBubbleClick(mapBubble));
        ((TextView) view.findViewById(R.id.name)).setText(mapBubble.getName());
        ((TextView) view.findViewById(R.id.status)).setText(mapBubble.getStatus());

        return view;
    }

    public interface OnMapBubbleClickListener {
        void onMapBubbleClick(MapBubble mapBubble);
    }
}
