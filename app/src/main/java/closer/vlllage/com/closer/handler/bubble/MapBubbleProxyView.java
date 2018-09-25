package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubbleProxyView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleProxyClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_suggestion, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onBubbleClick(mapBubble));
        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        TextView textView = view.findViewById(R.id.bubbleText);
        textView.setText(mapBubble.getStatus());
    }

    public interface MapBubbleProxyClickListener {
        void onBubbleClick(MapBubble mapBubble);
    }
}
