package closer.vlllage.com.closer.handler;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.TimeZone;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.text.format.DateUtils.DAY_IN_MILLIS;

public class EventHandler extends PoolMember {
    public void createNewEvent() {
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

                    alertConfig.setAlertResult(viewHolder);
                })
                .setPositiveButtonCallback(alertResult -> {
                    CreateEventViewHolder viewHolder = (CreateEventViewHolder) alertResult;
                })
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.post_event))
                .show();
    }

    private static class CreateEventViewHolder {
        TimePicker startsAtTimePicker;
        TimePicker endsAtTimePicker;
        DatePicker datePicker;
        TextView dateTextView;
        View changeDateButton;
        EditText eventName;
        EditText eventPrice;

        CreateEventViewHolder(View view) {
            this.startsAtTimePicker = view.findViewById(R.id.startsAt);
            this.endsAtTimePicker = view.findViewById(R.id.endsAt);
            this.datePicker = view.findViewById(R.id.datePicker);
            this.dateTextView = view.findViewById(R.id.dateTextView);
            this.changeDateButton = view.findViewById(R.id.changeDate);
            this.eventName = view.findViewById(R.id.name);
            this.eventPrice = view.findViewById(R.id.price);
        }
    }
}
