package closer.vlllage.com.closer.handler.map

import android.content.Intent
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_EVENT_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SUGGESTION
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_ZOOM
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.handler.map.MapHandler.Companion.DEFAULT_ZOOM
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class IntentHandler constructor(private val on: On) {

    fun onNewIntent(intent: Intent, onRequestMapOnScreenListener: (() -> Unit)?) {
        if (Intent.ACTION_VIEW == intent.action) {
            if (intent.hasExtra(EXTRA_EVENT_ID) || intent.hasExtra(EXTRA_GROUP_ID)) {
                val latLngFloats = intent.getDoubleArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats!![0], latLngFloats[1])
                on<MapHandler>().centerMap(latLng)
                onRequestMapOnScreenListener?.invoke()
            } else if (intent.hasExtra(EXTRA_SUGGESTION)) {
                val latLngFloats = intent.getDoubleArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats!![0], latLngFloats[1])

                val suggestion = Suggestion()
                suggestion.latitude = latLng.latitude
                suggestion.longitude = latLng.longitude
                suggestion.name = intent.getStringExtra(EXTRA_SUGGESTION)

                on<SuggestionHandler>().clearSuggestions()
                on<BubbleHandler>().remove { mapBubble -> BubbleType.MENU == mapBubble.type }

                on<BubbleHandler>().add(on<SuggestionHandler>().suggestionBubbleFrom(suggestion))
                on<MapHandler>().centerMap(latLng)
                onRequestMapOnScreenListener?.invoke()
            } else if (intent.hasExtra(EXTRA_LAT_LNG)) {
                val latLngFloats = intent.getDoubleArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats!![0], latLngFloats[1])

                if (intent.hasExtra(EXTRA_ZOOM)) {
                    on<MapHandler>().centerMap(latLng, intent.getFloatExtra(EXTRA_ZOOM, DEFAULT_ZOOM))
                    on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.approx_location))
                } else {
                    on<MapHandler>().centerMap(latLng)
                }

                onRequestMapOnScreenListener?.invoke()
            }
        }
    }
}
