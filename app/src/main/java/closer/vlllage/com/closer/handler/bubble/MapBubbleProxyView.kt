package closer.vlllage.com.closer.handler.bubble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import com.queatz.on.On

class MapBubbleProxyView constructor(private val on: On) {

    private lateinit var adapter: MapBubbleProxyAdapter

    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: (MapBubble) -> Unit): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_proxy, layer, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.bubbleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        adapter = MapBubbleProxyAdapter(on, onClickListener)
        recyclerView.adapter = adapter
        recyclerView.clipToOutline = true

        update(view, mapBubble)

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {
        adapter.items = mapBubble.proxies
    }
}
