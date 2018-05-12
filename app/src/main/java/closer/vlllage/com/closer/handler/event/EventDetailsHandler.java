package closer.vlllage.com.closer.handler.event;

import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;

public class EventDetailsHandler extends PoolMember {
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mma", Locale.US);

    public String formatEventDetails(Event event) {
        if (event.isCancelled()) {
            return $(ResourcesHandler.class).getResources().getString(R.string.cancelled);
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
            return $(ResourcesHandler.class).getResources()
                    .getString(R.string.event_price_and_time, event.getAbout(), eventTimeText);
        } else {
            return eventTimeText;
        }
    }

    public String formatRelative(Date date) {
        return DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                new Date().getTime(),
                MINUTE_IN_MILLIS
        ).toString();
    }
}
