package closer.vlllage.com.closer.handler.feed.content

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import com.queatz.on.On
import kotlinx.android.synthetic.main.welcome_item.view.*

class WelcomeMixedItem(val text: String, val cta: String? = null, val callback: (() -> Unit)? = null) : MixedItem(MixedItemType.Welcome)

class WelcomeViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Welcome) {
    val text = itemView.text!!
    val button = itemView.button!!
}

class WelcomeMixedItemAdapter(private val on: On) : MixedItemAdapter<WelcomeMixedItem, WelcomeViewHolder> {
    override fun bind(holder: WelcomeViewHolder, item: WelcomeMixedItem, position: Int) {
        bindWelcome(holder, item)
    }

    override fun getMixedItemClass() = WelcomeMixedItem::class
    override fun getMixedItemType() = MixedItemType.Welcome

    override fun areItemsTheSame(old: WelcomeMixedItem, new: WelcomeMixedItem) = false

    override fun areContentsTheSame(old: WelcomeMixedItem, new: WelcomeMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = WelcomeViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.welcome_item, parent, false))

    override fun onViewRecycled(holder: WelcomeViewHolder) {
    }

    private fun bindWelcome(holder: WelcomeViewHolder, item: WelcomeMixedItem) {
        holder.text.text = Html.fromHtml(item.text.replace("\n", "<br />"))

        holder.button.visible = !item.cta.isNullOrBlank()

        item.cta?.let {
            holder.button.text = it
            holder.button.setOnClickListener {
                item.callback?.invoke()
            }
        }
    }
}