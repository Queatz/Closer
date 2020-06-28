package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import com.queatz.on.On
import kotlinx.android.synthetic.main.text_item.view.*

class TextMixedItem(val text: String) : MixedItem(MixedItemType.Text)

class TextViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Text) {
    val text = itemView.text!!
}

class TextMixedItemAdapter(private val on: On) : MixedItemAdapter<TextMixedItem, TextViewHolder> {
    override fun bind(holder: TextViewHolder, item: TextMixedItem, position: Int) {
        bindText(holder, item.text)
    }

    override fun getMixedItemClass() = TextMixedItem::class
    override fun getMixedItemType() = MixedItemType.Text

    override fun areItemsTheSame(old: TextMixedItem, new: TextMixedItem) = false

    override fun areContentsTheSame(old: TextMixedItem, new: TextMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = TextViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.text_item, parent, false))

    override fun onViewRecycled(holder: TextViewHolder) {
    }

    private fun bindText(holder: TextViewHolder, text: String) {
        holder.text.text = text
    }
}