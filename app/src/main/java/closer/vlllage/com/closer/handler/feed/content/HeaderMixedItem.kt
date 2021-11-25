package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.databinding.FeedItemPublicGroupsBinding
import closer.vlllage.com.closer.handler.feed.PublicGroupFeedItemHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.map.FeedHandler
import com.queatz.on.On

class HeaderMixedItem : MixedItem(MixedItemType.Header)

class HeaderViewHolder(val binding: FeedItemPublicGroupsBinding): MixedItemViewHolder(binding.root, MixedItemType.Header) {
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

    override fun onCreateViewHolder(parent: ViewGroup) = HeaderViewHolder(FeedItemPublicGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: HeaderViewHolder) {
        holder.on.off()
    }

    private fun bindHeader(holder: HeaderViewHolder) {
        holder.on = On(on).apply { use<DisposableHandler>() }
        holder.on<PublicGroupFeedItemHandler>().attach(holder.binding) { on<FeedHandler>().show(it.value!!) }
    }
}