package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On
import kotlin.random.Random

class StoryMixedItem : MixedItem(MixedItemType.Story)

class StoryViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Story) {
    lateinit var on: On
}

class StoryMixedItemAdapter(private val on: On) : MixedItemAdapter<StoryMixedItem, StoryViewHolder> {
    override fun bind(holder: StoryViewHolder, item: StoryMixedItem, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.itemView.setBackgroundResource(listOf(
                R.color.red,
                R.color.green,
                R.color.colorPrimary,
                R.color.colorAccent,
        )[Random.nextInt(4)])
    }

    override fun getMixedItemClass() = StoryMixedItem::class
    override fun getMixedItemType() = MixedItemType.Story

    override fun areItemsTheSame(old: StoryMixedItem, new: StoryMixedItem) = true //old.group.objectBoxId == new.group.objectBoxId

    override fun areContentsTheSame(old: StoryMixedItem, new: StoryMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = StoryViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.story_item, parent, false))

    override fun onViewRecycled(holder: StoryViewHolder) {
        holder.on.off()
    }
}
