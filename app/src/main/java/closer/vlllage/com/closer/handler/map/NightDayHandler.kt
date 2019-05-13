package closer.vlllage.com.closer.handler.map

import android.location.Location

import com.luckycatlabs.sunrisesunset.Zenith
import com.luckycatlabs.sunrisesunset.calculator.SolarEventCalculator

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

import closer.vlllage.com.closer.pool.PoolMember

class NightDayHandler : PoolMember() {
    fun isNight(date: Date, location: Location): Boolean {
        val cal = Calendar.getInstance()
        cal.time = date

        val solarEventCalculator = SolarEventCalculator(com.luckycatlabs.sunrisesunset.dto.Location(location.latitude, location.longitude), TimeZone.getDefault())
        val sunrise = solarEventCalculator.computeSunriseCalendar(Zenith.CIVIL, cal).time
        val sunset = solarEventCalculator.computeSunsetCalendar(Zenith.CIVIL, cal).time

        return !(date.after(sunrise) && date.before(sunset))
    }
}
