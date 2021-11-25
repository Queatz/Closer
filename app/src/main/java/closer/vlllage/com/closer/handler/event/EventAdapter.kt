package closer.vlllage.com.closer.handler.event

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.CalendarEventItemBinding
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On

class EventAdapter(private val on: On) : RecyclerView.Adapter<EventViewHolder>() {

    var items = mutableListOf<Event>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EventViewHolder(
            CalendarEventItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = items[position]

        holder.itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight)
            marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
        }

        holder.binding.name.text = event.name
        holder.binding.about.text = on<EventDetailsHandler>().formatEventDetails(event)

        holder.binding.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (event.isPublic) R.drawable.ic_public_black_18dp else R.drawable.ic_group_black_18dp, 0, 0, 0
        )

        holder.itemView.setOnClickListener {
            on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event)
        }
    }

    override fun getItemCount() = items.size
}

class EventViewHolder(val binding: CalendarEventItemBinding) : RecyclerView.ViewHolder(binding.root)
