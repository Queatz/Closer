package closer.vlllage.com.closer.handler.bubble

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuItemAdapter.MenuItemViewHolder
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import java.util.*

class MapBubbleMenuItemAdapter(poolMember: PoolMember, private val mapBubble: MapBubble, private val onClickListener: MapBubbleMenuView.OnMapBubbleMenuItemClickListener) : PoolRecyclerAdapter<MenuItemViewHolder>(poolMember) {

    private val menuItems = ArrayList<MapBubbleMenuItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        return MenuItemViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.map_bubble_menu_item, parent, false))
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.menuItemTitle.text = menuItem.title

        holder.menuItemTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(menuItem.iconRes ?: 0, 0, 0, 0)

        holder.itemView.setOnClickListener { view -> onClickListener.onMenuItemClick(mapBubble, position) }
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    fun setMenuItems(menuItems: List<MapBubbleMenuItem>) {
        this.menuItems.clear()
        this.menuItems.addAll(menuItems)
        notifyDataSetChanged()
    }

    fun setMenuItems(vararg menuItems: MapBubbleMenuItem) {
        this.menuItems.clear()
        this.menuItems.addAll(Arrays.asList(*menuItems))
        notifyDataSetChanged()
    }

    inner class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var menuItemTitle: TextView

        init {
            menuItemTitle = itemView.findViewById(R.id.menuItemTitle)
        }
    }
}
