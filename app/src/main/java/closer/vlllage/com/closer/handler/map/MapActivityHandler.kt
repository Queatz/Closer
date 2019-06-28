package closer.vlllage.com.closer.handler.map

import android.content.Intent
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_EVENT_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PROMPT
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SCREEN
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SUGGESTION
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On

class MapActivityHandler constructor(private val on: On) {

    fun showSuggestionOnMap(suggestion: Suggestion) {
        val intent = Intent(on<ActivityHandler>().activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(suggestion.latitude!!.toFloat(), suggestion.longitude!!.toFloat()))
        intent.putExtra(EXTRA_SUGGESTION, if (suggestion.name == null) on<ResourcesHandler>().resources.getString(R.string.shared_location) else suggestion.name)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun showEventOnMap(event: Event) {
        val intent = Intent(on<ActivityHandler>().activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(event.latitude!!.toFloat(), event.longitude!!.toFloat()))
        intent.putExtra(EXTRA_EVENT_ID, event.id)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun showGroupOnMap(group: Group) {
        val intent = Intent(on<ActivityHandler>().activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(group.latitude!!.toFloat(), group.longitude!!.toFloat()))
        intent.putExtra(EXTRA_GROUP_ID, group.id)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun showPhoneOnMap(phoneId: String?) {
        if (phoneId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }
        val phone = on<StoreHandler>().store.box(Phone::class).query()
                .equal(Phone_.id, phoneId)
                .notNull(Phone_.latitude)
                .notNull(Phone_.longitude)
                .build()
                .findFirst()

        if (phone == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val intent = Intent(on<ActivityHandler>().activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, floatArrayOf(phone.latitude!!.toFloat(), phone.longitude!!.toFloat()))

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun goToScreen(screenName: String, message: String? = null) {
        val intent = Intent(on<ActivityHandler>().activity, MapsActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SCREEN, screenName)
        message?.let { intent.putExtra(EXTRA_PROMPT, it) }
        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
