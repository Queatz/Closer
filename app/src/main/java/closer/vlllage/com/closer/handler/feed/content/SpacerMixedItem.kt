package closer.vlllage.com.closer.handler.feed.content

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import androidx.annotation.ColorRes
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.WindowHandler
import com.queatz.on.On

class SpacerMixedItem(@ColorRes val color: Int = R.color.white) : MixedItem(MixedItemType.Spacer)

class SpacerViewHolder(val view: View, val parent: RecyclerView) : MixedItemViewHolder(view, MixedItemType.Spacer) {
    var scrollListener: RecyclerView.OnScrollListener? = null
    var layoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
}

class SpacerMixedItemAdapter(private val on: On) : MixedItemAdapter<SpacerMixedItem, SpacerViewHolder> {
    override fun bind(holder: SpacerViewHolder, item: SpacerMixedItem, position: Int) {
        holder.itemView.setBackgroundColor(on<ResourcesHandler>().resources.getColor(item.color))

        holder.scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                update(holder, true)
            }
        }

        holder.parent.addOnScrollListener(holder.scrollListener!!)

        holder.layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            update(holder, true)
        }

        holder.parent.viewTreeObserver.addOnGlobalLayoutListener(holder.layoutListener)
    }

    private fun update(holder: SpacerViewHolder, shrink: Boolean = false) {
        holder.view.updateLayoutParams {
            height = (
                holder.parent.measuredHeight - on<WindowHandler>().statusBarHeight -
                        if (shrink) (holder.parent.children.filter { it != holder.view }.sumOf { it.measuredHeight })
                        else 0
            ).coerceAtLeast(0)
        }
    }

    override fun getMixedItemClass() = SpacerMixedItem::class
    override fun getMixedItemType() = MixedItemType.Spacer

    override fun areItemsTheSame(old: SpacerMixedItem, new: SpacerMixedItem) = false

    override fun areContentsTheSame(old: SpacerMixedItem, new: SpacerMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = SpacerViewHolder(View(parent.context).apply {
        layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            height = (parent.measuredHeight - on<WindowHandler>().statusBarHeight).coerceAtLeast(0)
        }

        isClickable = true
        elevation = on<ResourcesHandler>().resources.getDimension(R.dimen.elevation)
    }, parent as RecyclerView)

    override fun onViewRecycled(holder: SpacerViewHolder) {
        holder.parent.viewTreeObserver.removeOnGlobalLayoutListener(holder.layoutListener!!)
        holder.parent.removeOnScrollListener(holder.scrollListener!!)
    }
}