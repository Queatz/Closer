package closer.vlllage.com.closer.handler.feed.content

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.WelcomeItemBinding
import closer.vlllage.com.closer.extensions.visible
import com.queatz.on.On

class WelcomeMixedItem(val text: String, val cta: String? = null, val callback: (() -> Unit)? = null) : MixedItem(MixedItemType.Welcome)

class WelcomeViewHolder(val binding: WelcomeItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.Welcome)


class WelcomeMixedItemAdapter(private val on: On) : MixedItemAdapter<WelcomeMixedItem, WelcomeViewHolder> {
    override fun bind(holder: WelcomeViewHolder, item: WelcomeMixedItem, position: Int) {
        bindWelcome(holder, item)
    }

    override fun getMixedItemClass() = WelcomeMixedItem::class
    override fun getMixedItemType() = MixedItemType.Welcome

    override fun areItemsTheSame(old: WelcomeMixedItem, new: WelcomeMixedItem) = false

    override fun areContentsTheSame(old: WelcomeMixedItem, new: WelcomeMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = WelcomeViewHolder(WelcomeItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: WelcomeViewHolder) {
    }

    private fun bindWelcome(holder: WelcomeViewHolder, item: WelcomeMixedItem) {
        holder.binding.text.text = Html.fromHtml(item.text.replace("\n", "<br />"))

        holder.binding.button.visible = !item.cta.isNullOrBlank()

        item.cta?.let {
            holder.binding.button.text = it
            holder.binding.button.setOnClickListener {
                item.callback?.invoke()
            }
        }
    }
}