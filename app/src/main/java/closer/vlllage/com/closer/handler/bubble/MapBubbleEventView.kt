package closer.vlllage.com.closer.handler.bubble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.OutboundHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.models.Event

class MapBubbleEventView constructor(private val on: On) {

    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: MapBubbleEventClickListener): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_event, layer, false)

        view.findViewById<View>(R.id.click).setOnClickListener { onClickListener.invoke(mapBubble) }
        update(view, mapBubble)

        view.findViewById<View>(R.id.directionsButton).setOnClickListener { on<OutboundHandler>().openDirections(mapBubble.latLng) }

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {
        val bubbleTextView = view.findViewById<TextView>(R.id.bubbleText)
        val actionTextView = view.findViewById<TextView>(R.id.action)
        bubbleTextView.text = mapBubble.status

        if ((mapBubble.tag as Event).isPublic) {
            bubbleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            bubbleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_group_black_18dp, 0, 0, 0)
        }

        actionTextView.text = on<EventDetailsHandler>().formatEventDetails(mapBubble.tag as Event)
    }
}

typealias MapBubbleEventClickListener = (mapBubble: MapBubble) -> Unit
