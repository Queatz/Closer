package closer.vlllage.com.closer.handler.bubble

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import com.queatz.on.On

class MapBubbleMenuView constructor(private val on: On) {
    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: OnMapBubbleMenuItemClickListener): View {
        val view = LayoutInflater.from(layer.context).inflate(R.layout.map_bubble_menu, layer, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.menuRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(
                layer.context,
                RecyclerView.VERTICAL,
                false
        )

        recyclerView.adapter = MapBubbleMenuItemAdapter(on, mapBubble, onClickListener)

        return view
    }

    fun getMenuAdapter(mapBubble: MapBubble): MapBubbleMenuItemAdapter {
        return (mapBubble.view!!.findViewById<View>(R.id.menuRecyclerView) as RecyclerView).adapter as MapBubbleMenuItemAdapter
    }

    fun setMenuTitle(mapBubble: MapBubble, title: String?) {
        val menuTitle = mapBubble.view!!.findViewById<TextView>(R.id.menuTitle)
        if (title?.isNotEmpty() == true) {
            menuTitle.visible = true
            menuTitle.text = title
        } else {
            menuTitle.visible = false
        }
    }

}

typealias OnMapBubbleMenuItemClickListener = (mapBubble: MapBubble, position: Int) -> Unit
