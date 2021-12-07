package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.ToolbarItemBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ToolbarAdapter(on: On, private val onToolbarItemSelected: (GroupToolbarHandler.ToolbarItem) -> Unit) : PoolRecyclerAdapter<ToolbarAdapter.ToolbarViewHolder>(on) {

    private var recyclerView: RecyclerView? = null
    val selectedContentView = BehaviorSubject.create<ContentViewType>()
    var isLight: Boolean = false

    var items = mutableListOf<GroupToolbarHandler.ToolbarItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
            recyclerView?.layoutManager?.isAutoMeasureEnabled = false
        }

    fun moveItem(from: Int, to: Int) {
        val item = items.removeAt(from)
        items.add(to, item)
        notifyItemMoved(from, to)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolbarViewHolder {
        return ToolbarViewHolder(ToolbarItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(viewHolder: ToolbarViewHolder, position: Int) {
        val item = items[position]

        viewHolder.binding.button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            item.icon, 0, 0, 0
        )

        viewHolder.binding.button.compoundDrawablePadding = 0
        viewHolder.binding.button.text = ""

        viewHolder.binding.button.setOnClickListener {
            item.onClickListener.onClick(it)
            onToolbarItemSelected.invoke(item)
        }

        viewHolder.disposableGroup = on<DisposableHandler>().group()
        viewHolder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            recolor(item, viewHolder.binding.button, it, selectedContentView.value)
        })

        selectedContentView.subscribe {
            recolor(item, viewHolder.binding.button, if (isLight) on<LightDarkHandler>().LIGHT else on<LightDarkHandler>().onLightChanged.value!!, it)
        }.also { viewHolder.disposableGroup.add(it) }

        item.indicator?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe { viewHolder.binding.indicator.visible = it }?.also {
                    viewHolder.disposableGroup.add(it)
                }
    }

    private fun recolor(item: GroupToolbarHandler.ToolbarItem, button: Button, colors: LightDarkColors, selected: ContentViewType?) {
        val isSelected = item.value == selected

        if (item.color != null && isSelected) {
            button.compoundDrawableTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(item.color!!))
            button.setTextColor(on<ResourcesHandler>().resources.getColor(R.color.textInverse))
            button.setTypeface(null, if (item.value == selected) Typeface.BOLD else Typeface.NORMAL)
        } else {
            button.compoundDrawableTintList = if (item.value == selected) colors.tintSelected else colors.tint
            button.setTextColor(if (item.value == selected) colors.selected else colors.text)
        }

        if (isSelected) {
            button.compoundDrawablePadding = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padHalf)
            button.text = item.name
        } else {
            button.compoundDrawablePadding = 0
            button.text = ""
        }

        button.animate()
                .scaleX(if (isSelected) 1.125f else 1f)
                .scaleY(if (isSelected) 1.125f else 1f)
                .setDuration(300)
                .setInterpolator(OvershootInterpolator(4f))
                .start()

        button.setBackgroundResource(colors.clickableRoundedBackgroundBorderless)
    }

    override fun onViewRecycled(holder: ToolbarViewHolder) {
        holder.disposableGroup.dispose()
    }

    override fun getItemCount() = items.size

    class ToolbarViewHolder(val binding: ToolbarItemBinding) : RecyclerView.ViewHolder(binding.root) {
        lateinit var disposableGroup: DisposableGroup
    }
}
