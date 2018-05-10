package closer.vlllage.com.closer.handler;

import android.support.v4.widget.NestedScrollView;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

public class EventHandler extends PoolMember {
    public void createNewEvent(final LatLng latLng) {
        $(AlertHandler.class).make()
                .setTheme(R.style.AppTheme_AlertDialog_Red)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.post_event))
                .setLayoutResId(R.layout.post_event_modal)
                .setOnAfterViewCreated((alertConfig, view) -> {
                    final CreateEventViewHolder viewHolder = new CreateEventViewHolder(view);

                    Calendar now = Calendar.getInstance(TimeZone.getDefault());
                    viewHolder.startsAtTimePicker.setCurrentHour((now.get(Calendar.HOUR_OF_DAY) + 1) % 24);
                    viewHolder.startsAtTimePicker.setCurrentMinute(0);
                    viewHolder.endsAtTimePicker.setCurrentHour((now.get(Calendar.HOUR_OF_DAY) + 4) % 24);
                    viewHolder.endsAtTimePicker.setCurrentMinute(0);
                    viewHolder.datePicker.setVisibility(View.GONE);

                    viewHolder.datePicker.setMinDate(now.getTimeInMillis());

                    viewHolder.datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), (datePicker1, year, month, dayOfMonth) -> {
                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                        calendar.set(year, month, dayOfMonth);
                        viewHolder.dateTextView.setText(DateUtils.getRelativeTimeSpanString(
                                calendar.getTimeInMillis(),
                                now.getTimeInMillis(),
                                DAY_IN_MILLIS
                        ));
                        viewHolder.changeDateButton.callOnClick();
                    });

                    viewHolder.changeDateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            viewHolder.datePicker.setVisibility(viewHolder.datePicker.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                        }
                    });

                    viewHolder.scrollView.setOnTouchListener(new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (viewHolder.eventName.hasFocus()) {
                                viewHolder.eventName.clearFocus();
                            }
                            if (viewHolder.eventPrice.hasFocus()) {
                                viewHolder.eventPrice.clearFocus();
                            }
                            return false;
                        }
                    });

                    alertConfig.setAlertResult(viewHolder);
                })
                .setButtonClickCallback(alertResult -> {
                    CreateEventViewHolder viewHolder = (CreateEventViewHolder) alertResult;
                    boolean isValid = new Date().before(getViewState(viewHolder).endsAt.getTime());
                    if (!isValid) {
                        $(DefaultAlerts.class).message($(ResourcesHandler.class).getResources().getString(R.string.event_must_not_end_in_the_past));
                    }
                    return isValid;
                })
                .setPositiveButtonCallback(alertResult -> {
                    CreateEventViewHolder viewHolder = (CreateEventViewHolder) alertResult;

                    CreateEventViewState event = getViewState(viewHolder);

                    createNewEvent(latLng,
                            viewHolder.eventName.getText().toString(),
                            viewHolder.eventPrice.getText().toString(),
                            event.startsAt.getTime(),
                            event.endsAt.getTime());
                })
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.post_event))
                .show();
    }

    private CreateEventViewState getViewState(CreateEventViewHolder viewHolder) {
        Calendar startsAt = Calendar.getInstance(TimeZone.getDefault());
        Calendar endsAt = Calendar.getInstance(TimeZone.getDefault());

        startsAt.set(viewHolder.datePicker.getYear(), viewHolder.datePicker.getMonth(), viewHolder.datePicker.getDayOfMonth(),
                viewHolder.startsAtTimePicker.getCurrentHour(), viewHolder.startsAtTimePicker.getCurrentMinute(), 0);
        endsAt.set(viewHolder.datePicker.getYear(), viewHolder.datePicker.getMonth(), viewHolder.datePicker.getDayOfMonth(),
                viewHolder.endsAtTimePicker.getCurrentHour(), viewHolder.endsAtTimePicker.getCurrentMinute(), 0);

        return new CreateEventViewState(startsAt, endsAt);
    }

    private void createNewEvent(LatLng latLng, String name, String price, Date startsAt, Date endsAt) {
        Event event = $(StoreHandler.class).create(Event.class);
        event.setName(name.trim());
        event.setAbout(price.trim());
        event.setLatitude(latLng.latitude);
        event.setLongitude(latLng.longitude);
        event.setStartsAt(startsAt);
        event.setEndsAt(endsAt);
        $(StoreHandler.class).getStore().box(Event.class).put(event);
        $(SyncHandler.class).sync(event);

        MapBubble mapBubble = eventBubbleFrom(event);
        $(MapHandler.class).centerMap(mapBubble.getLatLng());
    }

    public MapBubble eventBubbleFrom(Event event) {
        MapBubble mapBubble = new MapBubble(new LatLng(event.getLatitude(), event.getLongitude()), "Event", event.getName());
        mapBubble.setType(BubbleType.EVENT);
        mapBubble.setPinned(true);
        mapBubble.setTag(event);
        return mapBubble;
    }

    private static class CreateEventViewHolder {
        TimePicker startsAtTimePicker;
        TimePicker endsAtTimePicker;
        DatePicker datePicker;
        TextView dateTextView;
        View changeDateButton;
        EditText eventName;
        EditText eventPrice;
        NestedScrollView scrollView;

        CreateEventViewHolder(View view) {
            this.startsAtTimePicker = view.findViewById(R.id.startsAt);
            this.endsAtTimePicker = view.findViewById(R.id.endsAt);
            this.datePicker = view.findViewById(R.id.datePicker);
            this.dateTextView = view.findViewById(R.id.dateTextView);
            this.changeDateButton = view.findViewById(R.id.changeDate);
            this.eventName = view.findViewById(R.id.name);
            this.eventPrice = view.findViewById(R.id.price);
            scrollView = (NestedScrollView) view;
        }
    }

    private class CreateEventViewState {
        Calendar startsAt;
        Calendar endsAt;

        public CreateEventViewState(Calendar startsAt, Calendar endsAt) {
            this.startsAt = startsAt;
            this.endsAt = endsAt;
        }
    }
}
