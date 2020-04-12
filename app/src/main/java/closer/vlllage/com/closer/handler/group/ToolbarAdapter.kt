package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.GroupActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject

class ToolbarAdapter(on: On, private val onToolbarItemSelected: (GroupToolbarHandler.ToolbarItem) -> Unit) : PoolRecyclerAdapter<ToolbarAdapter.ToolbarViewHolder>(on) {

    private var recyclerView: RecyclerView? = null
    val selectedContentView = BehaviorSubject.create<GroupActivity.ContentViewType>()

    var items = mutableListOf<GroupToolbarHandler.ToolbarItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
            recyclerView?.layoutManager?.isAutoMeasureEnabled = false
        }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolbarViewHolder {
        return ToolbarViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.toolbar_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ToolbarViewHolder, position: Int) {
        val item = items[position]

        viewHolder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, item.icon, 0, 0
        )

        viewHolder.button.text = item.name

        viewHolder.button.setOnClickListener {
            item.onClickListener.onClick(it)
            onToolbarItemSelected.invoke(item)
        }

        viewHolder.disposableGroup = on<DisposableHandler>().group()
        viewHolder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            recolor(item, viewHolder.button, it, selectedContentView.value)
        })

        viewHolder.disposableGroup.add(selectedContentView.subscribe {
            recolor(item, viewHolder.button, on<LightDarkHandler>().onLightChanged.value!!, it)
        })
    }

    private fun recolor(item: GroupToolbarHandler.ToolbarItem, button: Button, colors: LightDarkColors, selected: GroupActivity.ContentViewType?) {
        button.compoundDrawableTintList = if (item.color !== null) ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(item.color!!)) else if (item.value == selected) colors.tintSelected else colors.tint
        button.setTextColor(if (item.color !== null) on<ResourcesHandler>().resources.getColor(R.color.textHintInverse) else if (item.value == selected) colors.selected else colors.text)
        button.setBackgroundResource(colors.clickableRoundedBackground8dp)
    }

    override fun onViewRecycled(holder: ToolbarViewHolder) {
        holder.disposableGroup.dispose()
    }

    override fun getItemCount() = items.size

    class ToolbarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.button)
        lateinit var disposableGroup: DisposableGroup
    }
}
