package closer.vlllage.com.closer.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.helpers.TimerHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import java.util.*

class CombinedRecyclerAdapter(poolMember: PoolMember) : PoolRecyclerAdapter<RecyclerView.ViewHolder>(poolMember) {

    private val adapters = ArrayList<RecyclerView.Adapter<RecyclerView.ViewHolder>>()
    private val adapterCursors = ArrayList<Int>()
    private val adapterItems = ArrayList<PriorityAdapterItem>()

    fun addAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        adapters.add(adapter)
        reset()
    }

    fun removeAdapter(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        adapters.remove(adapter)
        reset()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return adapterFromViewType(viewType).onCreateViewHolder(parent, viewType / 10000)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        advanceCursorToPosition(position)
        adapterItems[position].adapter!!.onBindViewHolder(holder, adapterItems[position].localPosition)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        adapterFromViewType(holder.itemViewType).onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        advanceCursorToPosition(position)
        return adapterItems[position].adapterIndex + 10000 * adapterItems[position].adapter!!.getItemViewType(adapterItems[position].localPosition)
    }

    override fun getItemCount(): Int {
        var count = 0

        for (adapter in adapters) {
            count += adapter.itemCount
        }

        return count
    }

    private fun adapterFromViewType(viewType: Int): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return adapters[viewType - viewType / 10000 * 10000]
    }

    private fun advanceCursorToPosition(position: Int) {
        if (adapters.isEmpty()) {
            return
        }

        while (adapterItems.size <= position) {
            adapterItems.add(findNextPriorityItem())
        }
    }

    private fun findNextPriorityItem(): PriorityAdapterItem {
        val priorityAdapterItem = PriorityAdapterItem()

        var priority = -1

        for (currentAdapterIndex in adapters.indices) {
            val adapter = adapters[currentAdapterIndex]

            if (adapterCursors[currentAdapterIndex] >= adapter.itemCount) {
                continue
            }

            val adapterPriority: Int
            if (adapter is PrioritizedAdapter) {
                adapterPriority = (adapter as PrioritizedAdapter).getItemPriority(adapterCursors[currentAdapterIndex])
            } else {
                adapterPriority = adapterCursors[currentAdapterIndex]
            }

            if (adapterPriority < 0) {
                throw IllegalStateException("Priority cannot be sub-zero")
            }

            if (adapterPriority < priority || priority == -1) {
                priorityAdapterItem.adapterIndex = currentAdapterIndex
                priorityAdapterItem.adapter = adapter
                priorityAdapterItem.localPosition = adapterCursors[currentAdapterIndex]
                priority = adapterPriority
            }
        }

        adapterCursors[priorityAdapterItem.adapterIndex] = adapterCursors[priorityAdapterItem.adapterIndex] + 1

        return priorityAdapterItem
    }

    private fun reset() {
        adapterCursors.clear()
        for (ignored in adapters) adapterCursors.add(0)
        adapterItems.clear()
        `$`(TimerHandler::class.java).post(Runnable { this.notifyDataSetChanged() })
    }

    fun notifyAdapterChanged(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        reset()
    }

    interface PrioritizedAdapter {
        fun getItemPriority(position: Int): Int
    }

    private class PriorityAdapterItem {
        internal var adapterIndex: Int = 0
        internal var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
        internal var localPosition: Int = 0
    }
}
