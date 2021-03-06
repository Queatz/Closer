package closer.vlllage.com.closer.handler.helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R

internal class MenuOptionAdapter(
        private val menuOptions: List<MenuHandler.MenuOption>,
        private val onMenuOptionClickListener: (menuOption: MenuHandler.MenuOption) -> Unit) : RecyclerView.Adapter<MenuOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.menu_modal_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val menuOption = menuOptions[position]
        menuOption.titleRes?.let { viewHolder.name.setText(it) }
        menuOption.title?.let { viewHolder.name.text = it }
        viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(menuOption.iconRes, 0, 0, 0)
        viewHolder.itemView.setOnClickListener { onMenuOptionClickListener.invoke(menuOption) }
    }

    override fun getItemCount() = menuOptions.size

    internal class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.name)
    }

}
