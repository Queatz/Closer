package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.GoalHandler
import closer.vlllage.com.closer.store.models.Goal
import com.queatz.on.On
import kotlinx.android.synthetic.main.item_goal.view.*

class GoalMixedItem(val goal: Goal) : MixedItem(MixedItemType.Goal)

class GoalViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Goal) {
    lateinit var on: On
}

class GoalMixedItemAdapter(private val on: On) : MixedItemAdapter<GoalMixedItem, GoalViewHolder> {
    override fun bind(holder: GoalViewHolder, item: GoalMixedItem, position: Int) {
        holder.itemView.card.setOnClickListener {
            on<GoalHandler>().show(item.goal.name!!)
        }

        holder.itemView.type.text = on<ResourcesHandler>().resources.getString(R.string.goal)
        holder.itemView.goalName.text = item.goal.name
        holder.itemView.cheerButton.text = on<ResourcesHandler>().resources.getString(R.string.tap_for_options)
        holder.itemView.count.visible = false

        val count = item.goal.phonesCount ?: 0
        holder.itemView.count.visible = count > 1
        holder.itemView.count.text = "$count"
    }

    override fun getMixedItemClass() = GoalMixedItem::class
    override fun getMixedItemType() = MixedItemType.Goal

    override fun areItemsTheSame(old: GoalMixedItem, new: GoalMixedItem): Boolean {
        return old.goal.id == new.goal.id
    }

    override fun areContentsTheSame(old: GoalMixedItem, new: GoalMixedItem): Boolean {
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup): GoalViewHolder {
        return GoalViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_goal, parent, false))
    }

    override fun onViewRecycled(holder: GoalViewHolder) {

    }
}
