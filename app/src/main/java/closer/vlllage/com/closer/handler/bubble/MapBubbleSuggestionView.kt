package closer.vlllage.com.closer.handler.bubble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.OutboundHandler
import com.queatz.on.On

class MapBubbleSuggestionView constructor(private val on: On) {
    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: MapBubbleSuggestionClickListener): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_suggestion, layer, false)

        view.findViewById<View>(R.id.click).setOnClickListener { v -> onClickListener.invoke(mapBubble) }
        update(view, mapBubble)

        view.findViewById<View>(R.id.directionsButton).setOnClickListener { v -> on<OutboundHandler>().openDirections(mapBubble.latLng) }

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {
        val text = view.findViewById<TextView>(R.id.bubbleText)
        text.text = mapBubble.status
        val action = view.findViewById<TextView>(R.id.action)
        action.text = mapBubble.action
    }

}

typealias MapBubbleSuggestionClickListener = (mapBubble: MapBubble) -> Unit
