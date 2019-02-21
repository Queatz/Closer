package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.OutboundHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubbleSuggestionView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleSuggestionClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_suggestion, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onSuggestionClick(mapBubble));
        update(view, mapBubble);

        view.findViewById(R.id.directionsButton).setOnClickListener(v -> $(OutboundHandler.class).openDirections(mapBubble.getLatLng()));

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        TextView text = view.findViewById(R.id.bubbleText);
        text.setText(mapBubble.getStatus());
        TextView action = view.findViewById(R.id.action);
        action.setText(mapBubble.getAction());
    }

    public interface MapBubbleSuggestionClickListener {
        void onSuggestionClick(MapBubble mapBubble);
    }
}
