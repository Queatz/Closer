package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuItemAdapter.MenuItemViewHolder
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On
import java.util.*

class MapBubbleMenuItemAdapter(on: On,
                               private val mapBubble: MapBubble,
                               private val onClickListener: OnMapBubbleMenuItemClickListener)
    : PoolRecyclerAdapter<MenuItemViewHolder>(on) {

    private val menuItems = mutableListOf<MapBubbleMenuItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.map_bubble_menu_item, parent, false))
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.menuItemTitle.text = menuItem.title

        holder.menuItemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(menuItem.iconRes ?: 0, 0, 0, 0)

        holder.menuItemTitle.compoundDrawableTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(menuItem.iconTintRes ?: R.color.textInverse))

        holder.itemView.setOnClickListener { view -> onClickListener.invoke(mapBubble, position) }
    }

    override fun getItemCount() = menuItems.size

    fun setMenuItems(menuItems: List<MapBubbleMenuItem>) {
        this.menuItems.clear()
        this.menuItems.addAll(menuItems)
        notifyDataSetChanged()
    }

    fun setMenuItems(vararg menuItems: MapBubbleMenuItem) {
        this.menuItems.clear()
        this.menuItems.addAll(listOf(*menuItems))
        notifyDataSetChanged()
    }

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var menuItemTitle: TextView = itemView.findViewById(R.id.menuItemTitle)
    }
}
