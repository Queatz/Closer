package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.handler.helpers.PhoneListActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.ReactionCount
import com.queatz.on.On

class ReactionAdapter(on: On) : PoolRecyclerAdapter<ReactionAdapter.ViewHolder>(on) {

    private var items: List<ReactionCount> = listOf()
    private var groupMessage: GroupMessage? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.reaction_item, viewGroup, false)).also {
            it.disposableGroup = on<DisposableHandler>().group()
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val reaction = items[position]

        viewHolder.reaction.text = "${reaction.reaction}${if (reaction.count < 2) "" else " ${reaction.count}"}"
        viewHolder.reaction.setOnClickListener {
            on<PhoneListActivityTransitionHandler>().showReactions(groupMessage!!.id!!)
        }

        viewHolder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            viewHolder.reaction.setTextColor(it.text)
            viewHolder.reaction.setBackgroundResource(it.clickableRoundedBackgroundBorderless)
        })
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.disposableGroup.clear()
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<ReactionCount>): ReactionAdapter {
        this.items = items
        notifyDataSetChanged()
        return this
    }

    fun setGroupMessage(groupMessage: GroupMessage) {
        this.groupMessage = groupMessage
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var reaction: TextView = itemView as TextView
        internal lateinit var disposableGroup: DisposableGroup
    }
}
