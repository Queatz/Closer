package closer.vlllage.com.closer.handler.group

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.PhoneListActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.ReactionCount
import java.util.*

class ReactionAdapter(poolMember: PoolMember) : PoolRecyclerAdapter<ReactionAdapter.ViewHolder>(poolMember) {

    private var items: List<ReactionCount> = ArrayList()
    private var groupMessage: GroupMessage? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.reaction_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val reaction = items[position]

        viewHolder.reaction.text = reaction.reaction + " " + reaction.count
        viewHolder.reaction.setOnClickListener { v -> `$`(PhoneListActivityTransitionHandler::class.java).showReactions(groupMessage!!.id!!) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItems(items: List<ReactionCount>): ReactionAdapter {
        this.items = items
        notifyDataSetChanged()
        return this
    }

    fun setGroupMessage(groupMessage: GroupMessage) {
        this.groupMessage = groupMessage
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var reaction: TextView

        init {
            reaction = itemView as TextView
        }
    }
}
