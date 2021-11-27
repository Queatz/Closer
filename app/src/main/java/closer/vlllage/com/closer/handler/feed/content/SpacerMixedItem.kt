package closer.vlllage.com.closer.handler.feed.content

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.dpToPx
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On

class SpacerMixedItem : MixedItem(MixedItemType.Spacer)

class SpacerViewHolder(val view: View, val parent: RecyclerView) : MixedItemViewHolder(view, MixedItemType.Spacer) {
    var scrollListener: RecyclerView.OnScrollListener? = null
    var listener: View.OnLayoutChangeListener? = null
}

class SpacerMixedItemAdapter(private val on: On) : MixedItemAdapter<SpacerMixedItem, SpacerViewHolder> {
    override fun bind(holder: SpacerViewHolder, item: SpacerMixedItem, position: Int) {
        holder.view.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            height = (
                holder.parent.measuredHeight - 96.dpToPx(holder.view.context)
            ).coerceAtLeast(0)
        }

        holder.listener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            holder.view.updateLayoutParams {
                height = (
                        holder.parent.measuredHeight - 96.dpToPx(holder.view.context)
                ).coerceAtLeast(0)
            }
        }

        holder.scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                holder.view.updateLayoutParams {
                    height = (
                        holder.parent.measuredHeight - 96.dpToPx(holder.view.context) - (holder.parent.children.filter { it != holder.view }.sumOf { it.measuredHeight })
                    ).coerceAtLeast(0)
                }
            }
        }

        holder.parent.addOnScrollListener(holder.scrollListener!!)
        holder.parent.addOnLayoutChangeListener(holder.listener)
    }

    override fun getMixedItemClass() = SpacerMixedItem::class
    override fun getMixedItemType() = MixedItemType.Spacer

    override fun areItemsTheSame(old: SpacerMixedItem, new: SpacerMixedItem) = false

    override fun areContentsTheSame(old: SpacerMixedItem, new: SpacerMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = SpacerViewHolder(View(parent.context).apply {
        setBackgroundColor(on<ResourcesHandler>().resources.getColor(R.color.white))
        isClickable = true
        elevation = on<ResourcesHandler>().resources.getDimension(R.dimen.elevation)
    }, parent as RecyclerView)

    override fun onViewRecycled(holder: SpacerViewHolder) {
        holder.parent.removeOnLayoutChangeListener(holder.listener)
    }
}