package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.PhoneListActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.ReactionCount
import com.queatz.on.On
import java.util.*

class ReactionAdapter(on: On) : PoolRecyclerAdapter<ReactionAdapter.ViewHolder>(on) {

    private var items: List<ReactionCount> = ArrayList()
    private var groupMessage: GroupMessage? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.reaction_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val reaction = items[position]

        viewHolder.reaction.text = reaction.reaction + " " + reaction.count
        viewHolder.reaction.setOnClickListener { v -> on<PhoneListActivityTransitionHandler>().showReactions(groupMessage!!.id!!) }
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
        internal var reaction: TextView = itemView as TextView

    }
}