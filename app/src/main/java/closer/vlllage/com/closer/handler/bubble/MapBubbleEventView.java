package closer.vlllage.com.closer.handler.bubble;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.OutboundHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

public class MapBubbleEventView extends PoolMember {

    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma", Locale.US);

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

        Event event = (Event) mapBubble.getTag();

        if (event.isCancelled()) {
            actionTextView.setText($(ResourcesHandler.class).getResources().getString(R.string.cancelled));
            return;
        }

        timeFormatter.setTimeZone(TimeZone.getDefault());
        String startTime = timeFormatter.format(event.getStartsAt());
        String endTime = timeFormatter.format(event.getEndsAt());
        String day = DateUtils.getRelativeTimeSpanString(
                event.getStartsAt().getTime(),
                new Date().getTime(),
                DAY_IN_MILLIS
        ).toString();

        String eventTimeText = $(ResourcesHandler.class).getResources()
                .getString(R.string.event_start_end_time, startTime, endTime, day);

        if (event.getAbout() != null && !event.getAbout().trim().isEmpty()) {
            actionTextView.setText($(ResourcesHandler.class).getResources()
                    .getString(R.string.event_price_and_time, eventTimeText));
        } else {
            actionTextView.setText(eventTimeText);
        }
    }

    public interface MapBubbleEventClickListener {
        void onEventClick(MapBubble mapBubble);
    }
}
