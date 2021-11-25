package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.FeedItemGoalBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.GoalHandler
import closer.vlllage.com.closer.store.models.Goal
import com.queatz.on.On

class GoalMixedItem(val goal: Goal) : MixedItem(MixedItemType.Goal)

class GoalViewHolder(val binding: FeedItemGoalBinding) : MixedItemViewHolder(binding.root, MixedItemType.Goal) {
    lateinit var on: On
}

class GoalMixedItemAdapter(private val on: On) : MixedItemAdapter<GoalMixedItem, GoalViewHolder> {
    override fun bind(holder: GoalViewHolder, item: GoalMixedItem, position: Int) {
        holder.binding.itemGoal.card.setOnClickListener {
            on<GoalHandler>().show(item.goal.name!!)
        }

        holder.binding.itemGoal.type.text = on<ResourcesHandler>().resources.getString(R.string.goal)
        holder.binding.itemGoal.goalName.text = item.goal.name
        holder.binding.itemGoal.cheerButton.text = on<ResourcesHandler>().resources.getString(R.string.tap_for_options)
        holder.binding.itemGoal.count.visible = false

        val count = item.goal.phonesCount ?: 0
        holder.binding.itemGoal.count.visible = true
        holder.binding.itemGoal.count.text = "$count"
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
        return GoalViewHolder(FeedItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onViewRecycled(holder: GoalViewHolder) {

    }
}
