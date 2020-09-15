package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.LifestyleHandler
import closer.vlllage.com.closer.store.models.Lifestyle
import com.queatz.on.On
import kotlinx.android.synthetic.main.item_goal.view.*

class LifestyleMixedItem(val lifestyle: Lifestyle) : MixedItem(MixedItemType.Lifestyle)

class LifestyleViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Lifestyle) {
    lateinit var on: On
}

class LifestyleMixedItemAdapter(private val on: On) : MixedItemAdapter<LifestyleMixedItem, LifestyleViewHolder> {
    override fun bind(holder: LifestyleViewHolder, item: LifestyleMixedItem, position: Int) {
        holder.itemView.card.setOnClickListener {
            on<LifestyleHandler>().show(item.lifestyle.name!!)
        }

        holder.itemView.type.text = on<ResourcesHandler>().resources.getString(R.string.lifestyle)
        holder.itemView.goalName.text = item.lifestyle.name
        holder.itemView.cheerButton.text = on<ResourcesHandler>().resources.getString(R.string.tap_for_options)
        holder.itemView.count.visible = false

        val count = item.lifestyle.phonesCount ?: 0
        holder.itemView.count.visible = count > 1
        holder.itemView.count.text = "$count"
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
        return return LifestyleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.feed_item_goal, parent, false))
    }

    override fun onViewRecycled(holder: LifestyleViewHolder) {

    }
}
