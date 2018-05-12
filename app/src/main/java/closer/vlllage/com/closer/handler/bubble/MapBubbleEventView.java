package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.OutboundHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;

public class MapBubbleEventView extends PoolMember {

    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleEventClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_event, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onEventClick(mapBubble));
        update(view, mapBubble);

        view.findViewById(R.id.directionsButton).setOnClickListener(v -> $(OutboundHandler.class).openDirections(mapBubble.getLatLng()));

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        TextView bubbleTextView = view.findViewById(R.id.bubbleText);
        TextView actionTextView = view.findViewById(R.id.action);
        bubbleTextView.setText(mapBubble.getStatus());

        actionTextView.setText($(EventDetailsHandler.class).formatEventDetails((Event) mapBubble.getTag()));
    }

    public interface MapBubbleEventClickListener {
        void onEventClick(MapBubble mapBubble);
    }
}
