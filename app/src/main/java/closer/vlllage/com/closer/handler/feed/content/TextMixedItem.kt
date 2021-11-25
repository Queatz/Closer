package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.databinding.TextItemBinding
import com.queatz.on.On

class TextMixedItem(val text: String) : MixedItem(MixedItemType.Text)

class TextViewHolder(val binding: TextItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.Text) {
}

class TextMixedItemAdapter(private val on: On) : MixedItemAdapter<TextMixedItem, TextViewHolder> {
    override fun bind(holder: TextViewHolder, item: TextMixedItem, position: Int) {
        bindText(holder, item.text)
    }

    override fun getMixedItemClass() = TextMixedItem::class
    override fun getMixedItemType() = MixedItemType.Text

    override fun areItemsTheSame(old: TextMixedItem, new: TextMixedItem) = false

    override fun areContentsTheSame(old: TextMixedItem, new: TextMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = TextViewHolder(TextItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: TextViewHolder) {
    }

    private fun bindText(holder: TextViewHolder, text: String) {
        holder.binding.text.text = text
    }
}