package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.FeedItemGoalBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.LifestyleHandler
import closer.vlllage.com.closer.store.models.Lifestyle
import com.queatz.on.On

class LifestyleMixedItem(val lifestyle: Lifestyle) : MixedItem(MixedItemType.Lifestyle)

class LifestyleViewHolder(val binding: FeedItemGoalBinding) : MixedItemViewHolder(binding.root, MixedItemType.Lifestyle) {
    lateinit var on: On
}

class LifestyleMixedItemAdapter(private val on: On) : MixedItemAdapter<LifestyleMixedItem, LifestyleViewHolder> {
    override fun bind(holder: LifestyleViewHolder, item: LifestyleMixedItem, position: Int) {
        holder.binding.itemGoal.card.setOnClickListener {
            on<LifestyleHandler>().show(item.lifestyle.name!!)
        }

        holder.binding.itemGoal.type.text = on<ResourcesHandler>().resources.getString(R.string.lifestyle)
        holder.binding.itemGoal.goalName.text = item.lifestyle.name
        holder.binding.itemGoal.cheerButton.text = on<ResourcesHandler>().resources.getString(R.string.tap_for_options)
        holder.binding.itemGoal.count.visible = false

        val count = item.lifestyle.phonesCount ?: 0
        holder.binding.itemGoal.count.visible = true
        holder.binding.itemGoal.count.text = "$count"
    }

    override fun getMixedItemClass() = LifestyleMixedItem::class
    override fun getMixedItemType() = MixedItemType.Lifestyle

    override fun areItemsTheSame(old: LifestyleMixedItem, new: LifestyleMixedItem): Boolean {
        return old.lifestyle.id == new.lifestyle.id
    }

    override fun areContentsTheSame(old: LifestyleMixedItem, new: LifestyleMixedItem): Boolean {
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup): LifestyleViewHolder {
        return return LifestyleViewHolder(FeedItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onViewRecycled(holder: LifestyleViewHolder) {

    }
}
