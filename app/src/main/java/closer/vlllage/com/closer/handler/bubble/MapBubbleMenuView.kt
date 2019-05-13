package closer.vlllage.com.closer.handler.bubble

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember

class MapBubbleMenuView : PoolMember() {
    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: OnMapBubbleMenuItemClickListener): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_menu, layer, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.menuRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(
                layer.context,
                RecyclerView.VERTICAL,
                false
        )

        recyclerView.adapter = MapBubbleMenuItemAdapter(this, mapBubble, onClickListener)

        return view
    }

    fun getMenuAdapter(mapBubble: MapBubble): MapBubbleMenuItemAdapter {
        return (mapBubble.view!!.findViewById<View>(R.id.menuRecyclerView) as RecyclerView).adapter as MapBubbleMenuItemAdapter
    }

    fun setMenuTitle(mapBubble: MapBubble, title: String?) {
        val menuTitle = mapBubble.view!!.findViewById<TextView>(R.id.menuTitle)
        if (title == null || title.isEmpty()) {
            menuTitle.visibility = View.GONE
        } else {
            menuTitle.visibility = View.VISIBLE
            menuTitle.text = title
        }
    }

    interface OnMapBubbleMenuItemClickListener {
        fun onMenuItemClick(mapBubble: MapBubble, position: Int)
    }
}
