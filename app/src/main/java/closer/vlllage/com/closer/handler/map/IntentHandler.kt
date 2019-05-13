package closer.vlllage.com.closer.handler.map

import android.content.Intent
import closer.vlllage.com.closer.GroupActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_EVENT_ID
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_NAME
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_PHONE
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_STATUS
import closer.vlllage.com.closer.MapsActivity.Companion.EXTRA_SUGGESTION
import closer.vlllage.com.closer.handler.bubble.BubbleHandler
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.model.LatLng

class IntentHandler : PoolMember() {

    fun onNewIntent(intent: Intent, onRequestMapOnScreenListener: (() -> Unit)?) {
        if (Intent.ACTION_VIEW == intent.action) {
            if (intent.hasExtra(EXTRA_STATUS)) {
                val latLng = intent.getFloatArrayExtra(EXTRA_LAT_LNG)
                `$`(ReplyLayoutHandler::class.java).replyTo(MapBubble(
                        if (intent.hasExtra(EXTRA_LAT_LNG)) LatLng(latLng[0].toDouble(), latLng[1].toDouble()) else null,
                        if (intent.hasExtra(EXTRA_NAME)) intent.getStringExtra(EXTRA_NAME) else "",
                        intent.getStringExtra(EXTRA_STATUS)
                ).apply {
                    phone = intent.getStringExtra(EXTRA_PHONE)
                })
                onRequestMapOnScreenListener?.invoke()
            } else if (intent.hasExtra(EXTRA_EVENT_ID) || intent.hasExtra(EXTRA_GROUP_ID)) {
                val latLngFloats = intent.getFloatArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats[0].toDouble(), latLngFloats[1].toDouble())
                `$`(MapHandler::class.java).centerMap(latLng)
                onRequestMapOnScreenListener?.invoke()
            } else if (intent.hasExtra(EXTRA_SUGGESTION)) {
                val latLngFloats = intent.getFloatArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats[0].toDouble(), latLngFloats[1].toDouble())

                val suggestion = Suggestion()
                suggestion.latitude = latLng.latitude
                suggestion.longitude = latLng.longitude
                suggestion.name = intent.getStringExtra(EXTRA_SUGGESTION)

                `$`(SuggestionHandler::class.java).clearSuggestions()
                `$`(BubbleHandler::class.java).remove { mapBubble -> BubbleType.MENU == mapBubble.type }

                `$`(BubbleHandler::class.java).add(`$`(SuggestionHandler::class.java).suggestionBubbleFrom(suggestion))
                `$`(MapHandler::class.java).centerMap(latLng)
                onRequestMapOnScreenListener?.invoke()
            } else if (intent.hasExtra(EXTRA_LAT_LNG)) {
                val latLngFloats = intent.getFloatArrayExtra(EXTRA_LAT_LNG)
                val latLng = LatLng(latLngFloats[0].toDouble(), latLngFloats[1].toDouble())
                `$`(MapHandler::class.java).centerMap(latLng)
                onRequestMapOnScreenListener?.invoke()
            }
        }
    }
}
