package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubbleView {
    public static View from(ViewGroup layer, MapBubble mapBubble, OnMapBubbleClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onMapBubbleClick(mapBubble));
        update(view, mapBubble);

        return view;
    }

    public static void update(View view, MapBubble mapBubble) {
        if (mapBubble.getName().isEmpty()) {
            view.findViewById(R.id.name).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.name).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.name)).setText(mapBubble.getName());
        }

        ((TextView) view.findViewById(R.id.status)).setText(mapBubble.getStatus());

        if (mapBubble.getAction() != null) {
            ((TextView) view.findViewById(R.id.action)).setText(mapBubble.getAction());
        }
    }

    public interface OnMapBubbleClickListener {
        void onMapBubbleClick(MapBubble mapBubble);
    }
}
