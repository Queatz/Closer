package closer.vlllage.com.closer.handler.map;

import android.location.Location;

import com.luckycatlabs.sunrisesunset.Zenith;
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import closer.vlllage.com.closer.pool.PoolMember;

public class NightDayHandler extends PoolMember {
    public boolean isNight(Date date, Location location) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        SolarEventCalculator solarEventCalculator = new SolarEventCalculator(new com.luckycatlabs.sunrisesunset.dto.Location(location.getLatitude(), location.getLongitude()), TimeZone.getDefault());
        Date sunrise = solarEventCalculator.computeSunriseCalendar(Zenith.CIVIL, cal).getTime();
        Date sunset = solarEventCalculator.computeSunsetCalendar(Zenith.CIVIL, cal).getTime();

        return !(date.after(sunrise) && date.before(sunset));
    }
}
