package closer.vlllage.com.closer.handler.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.models.Event
import com.queatz.on.On
import kotlinx.android.synthetic.main.calendar_event_item.view.*

class EventAdapter(private val on: On) : RecyclerView.Adapter<EventViewHolder>() {

    var items = mutableListOf<Event>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EventViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.calendar_event_item, parent, false)
    )

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = items[position]

        holder.itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            height = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight)
            marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
        }

        holder.name.text = event.name
        holder.about.text = on<EventDetailsHandler>().formatEventDetails(event)

        holder.itemView.setOnClickListener {
            on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event)
        }
    }

    override fun getItemCount() = items.size
}

class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name = view.name!!
    val about = view.about!!
}
