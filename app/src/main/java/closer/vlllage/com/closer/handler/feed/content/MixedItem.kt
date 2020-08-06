package closer.vlllage.com.closer.handler.feed.content

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

enum class MixedItemType {
    CalendarDay,
    GroupPreview,
    GroupMessage,
    Quest,
    GroupAction,
    Header,
    Text,
    Notification,
    MessageContact,
}

abstract class MixedItem(val type: MixedItemType)

abstract class MixedItemViewHolder(itemView: View, val type: MixedItemType) : RecyclerView.ViewHolder(itemView)

interface MixedItemAdapter<T : MixedItem, VH : MixedItemViewHolder> {
    fun bind(holder: VH, item: T, position: Int)
    fun getMixedItemClass(): KClass<out MixedItem>
    fun getMixedItemType(): MixedItemType
    fun areItemsTheSame(old: T, new: T): Boolean
    fun areContentsTheSame(old: T, new: T): Boolean
    fun onCreateViewHolder(parent: ViewGroup): VH
    fun onViewRecycled(holder: VH)
}