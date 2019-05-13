package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import java.util.*

internal class ToolbarAdapter(poolMember: PoolMember) : PoolRecyclerAdapter<ToolbarAdapter.ToolbarViewHolder>(poolMember) {

    private val items = ArrayList<GroupToolbarHandler.ToolbarItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolbarViewHolder {
        return ToolbarViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.toolbar_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ToolbarViewHolder, position: Int) {
        val item = items[position]

        viewHolder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, item.icon, 0, 0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.button.compoundDrawableTintList = ColorStateList.valueOf(
                    `$`(ResourcesHandler::class.java).resources.getColor(R.color.text)
            )
        }

        viewHolder.button.setText(item.name)

        viewHolder.button.setOnClickListener(item.onClickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<GroupToolbarHandler.ToolbarItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    internal class ToolbarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button

        init {
            button = itemView.findViewById(R.id.button)
        }
    }
}
