package closer.vlllage.com.closer.handler.helpers;

import java.util.Date;

import closer.vlllage.com.closer.pool.PoolMember;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class TimeAgo extends PoolMember {
    public Date fifteenDaysAgo() {
        Date fifteenDaysAgo = new Date();
        fifteenDaysAgo.setTime(fifteenDaysAgo.getTime() - 15 * DAY_IN_MILLIS);
        return fifteenDaysAgo;
    }

    public Date thirtySixHoursAgo() {
        Date thirtySixHoursAgo = new Date();
        thirtySixHoursAgo.setTime(thirtySixHoursAgo.getTime() - 36 * HOUR_IN_MILLIS);
        return thirtySixHoursAgo;
    }

    public Date oneMonthAgo() {
        Date oneMonthAgo = new Date();
        oneMonthAgo.setTime(oneMonthAgo.getTime() - 30 * DAY_IN_MILLIS);
        return oneMonthAgo;
    }
}
