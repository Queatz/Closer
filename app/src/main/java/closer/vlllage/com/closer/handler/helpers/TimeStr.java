package closer.vlllage.com.closer.handler.helpers;

import android.text.format.DateUtils;

import java.util.Date;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class TimeStr extends PoolMember {

    public String pretty(Date date) {
        if (date == null) {
            return "-";
        }

        if (new Date().getTime() - date.getTime() < 5 * MINUTE_IN_MILLIS) {
            return $(ResourcesHandler.class).getResources().getString(R.string.just_now);
        }

        if (DateUtils.isToday(date.getTime())) {
            return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
        }

        return DateUtils.getRelativeDateTimeString(
                $(ApplicationHandler.class).getApp(),
                date.getTime(),
                MINUTE_IN_MILLIS,
                WEEK_IN_MILLIS,
                0
        ).toString();
    }

    public String prettyDate(Date date) {
        if (date == null) {
            return "-";
        }

        if (DateUtils.isToday(date.getTime())) {
            return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
        }

        return DateUtils.formatDateTime(
                $(ApplicationHandler.class).getApp(),
                date.getTime(),
                0
        );
    }
}
