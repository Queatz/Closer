package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.feed.PublicGroupFeedItemHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.FeedHandler
import com.queatz.on.On

class HeaderMixedItem : MixedItem(MixedItemType.Header)

class HeaderViewHolder(val view: ViewGroup): MixedItemViewHolder(view, MixedItemType.Header) {
    lateinit var on: On
}

class HeaderMixedItemAdapter(private val on: On) : MixedItemAdapter<HeaderMixedItem, HeaderViewHolder> {
    override fun bind(holder: HeaderViewHolder, item: HeaderMixedItem, position: Int) {
        bindHeader(holder)
    }

    override fun getMixedItemClass() = HeaderMixedItem::class
    override fun getMixedItemType() = MixedItemType.Header

    override fun areItemsTheSame(old: HeaderMixedItem, new: HeaderMixedItem) = true

    override fun areContentsTheSame(old: HeaderMixedItem, new: HeaderMixedItem) = true

    override fun onCreateViewHolder(parent: ViewGroup) = HeaderViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.feed_item_public_groups, parent, false) as ViewGroup)

    override fun onViewRecycled(holder: HeaderViewHolder) {
        holder.on.off()
    }

    private fun bindHeader(holder: HeaderViewHolder) {
        holder.on = On(on).apply { use<DisposableHandler>() }
        holder.on<PublicGroupFeedItemHandler>().attach(holder.view) { on<FeedHandler>().show(it) }
    }
}