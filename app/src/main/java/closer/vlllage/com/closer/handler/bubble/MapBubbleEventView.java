package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.OutboundHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubbleEventView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleEventClickListener onClickListener) {
        android.view.View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_event, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onEventClick(mapBubble));
        update(view, mapBubble);

        ((TextView) view.findViewById(R.id.action)).setText("8:15pm - 11:45pm Today");

        view.findViewById(R.id.directionsButton).setOnClickListener(v -> $(OutboundHandler.class).openDirections(mapBubble.getLatLng()));

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        TextView textView = view.findViewById(R.id.bubbleText);
        textView.setText(mapBubble.getStatus());
    }

    public interface MapBubbleEventClickListener {
        void onEventClick(MapBubble mapBubble);
    }
}
