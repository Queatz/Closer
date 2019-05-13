package closer.vlllage.com.closer.handler.bubble

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember

class MapBubbleProxyView : PoolMember() {

    private var adapter: MapBubbleProxyAdapter? = null

    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: (MapBubble) -> Unit): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_proxy, layer, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.bubbleRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        adapter = MapBubbleProxyAdapter(this, mapBubble, onClickListener)
        recyclerView.adapter = adapter

        update(view, mapBubble)

        return view
    }

    fun update(view: View, mapBubble: MapBubble) {
        adapter!!.setItems(mapBubble.proxies)
    }
}
