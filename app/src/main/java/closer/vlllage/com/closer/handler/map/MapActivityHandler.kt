package closer.vlllage.com.closer.handler.map

import android.content.Intent
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_EVENT_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_NAME
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SCREEN
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_STATUS
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SUGGESTION
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng

class MapActivityHandler : PoolMember() {

    fun showSuggestionOnMap(suggestion: Suggestion) {
        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(suggestion.latitude!!.toFloat(), suggestion.longitude!!.toFloat()))
        intent.putExtra(EXTRA_SUGGESTION, if (suggestion.name == null) `$`(ResourcesHandler::class.java).resources.getString(R.string.shared_location) else suggestion.name)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun showEventOnMap(event: Event) {
        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(event.latitude!!.toFloat(), event.longitude!!.toFloat()))
        intent.putExtra(EXTRA_EVENT_ID, event.id)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun showGroupOnMap(group: Group) {
        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(group.latitude!!.toFloat(), group.longitude!!.toFloat()))
        intent.putExtra(EXTRA_GROUP_ID, group.id)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun showPhoneOnMap(phoneId: String?) {
        if (phoneId == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }
        val phone = `$`(StoreHandler::class.java).store.box(Phone::class.java).query()
                .equal(Phone_.id, phoneId)
                .notNull(Phone_.latitude)
                .notNull(Phone_.longitude)
                .build()
                .findFirst()

        if (phone == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(phone.latitude!!.toFloat(), phone.longitude!!.toFloat()))

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun replyToPhone(phoneId: String, name: String, status: String, latLng: LatLng?) {
        var name = name
        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        if (latLng != null) {
            intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(latLng.latitude.toFloat(), latLng.longitude.toFloat()))
        }

        name = if (name.isEmpty())
            `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name)
        else
            name

        intent.putExtra(EXTRA_NAME, name)
        intent.putExtra(EXTRA_STATUS, status)
        intent.putExtra(EXTRA_PHONE, phoneId)

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }

    fun goToScreen(screenName: String) {
        val intent = Intent(`$`(ActivityHandler::class.java).activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SCREEN, screenName)
        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)

    }
}
