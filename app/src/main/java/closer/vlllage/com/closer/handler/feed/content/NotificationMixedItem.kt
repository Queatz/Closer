package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.NotificationItemBinding
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.Notification
import com.queatz.on.On

class NotificationMixedItem(val notification: Notification) : MixedItem(MixedItemType.Notification)

class NotificationViewHolder(val binding: NotificationItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.Notification) {
    lateinit var on: On
    lateinit var disposableGroup: DisposableGroup
}

class NotificationMixedItemAdapter(private val on: On) : MixedItemAdapter<NotificationMixedItem, NotificationViewHolder> {
    override fun bind(holder: NotificationViewHolder, item: NotificationMixedItem, position: Int) {
        bindNotification(holder, item.notification)
    }

    override fun getMixedItemClass() = NotificationMixedItem::class
    override fun getMixedItemType() = MixedItemType.Notification

    override fun areItemsTheSame(old: NotificationMixedItem, new: NotificationMixedItem) = old.notification.objectBoxId == new.notification.objectBoxId

    override fun areContentsTheSame(old: NotificationMixedItem, new: NotificationMixedItem) = true

    override fun onCreateViewHolder(parent: ViewGroup) = NotificationViewHolder(NotificationItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: NotificationViewHolder) {
        holder.on.off()
    }

    private fun bindNotification(holder: NotificationViewHolder, notification: Notification) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }
        holder.binding.notificationName.text = notification.name ?: ""
        holder.binding.notificationMessage.text = notification.message ?: ""
        holder.binding.notificationTime.text = on<TimeStr>().prettyDate(notification.created)
        holder.itemView.setOnClickListener {
            on<NotificationHandler>().launch(notification)
        }

        holder.itemView.setOnLongClickListener {
            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.sent_at, on<TimeStr>().pretty(notification.created)))
            true
        }
    }
}