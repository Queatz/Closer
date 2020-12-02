package closer.vlllage.com.closer.handler.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.handler.helpers.TimeStr
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import kotlinx.android.synthetic.main.person_item.view.*

class PeopleAdapter(private val on: On, private val small: Boolean = false) : RecyclerView.Adapter<PeopleViewHolder>() {

    var people = mutableListOf<Phone>()
        set(value) {
            if (field.isEmpty()) {
                field.addAll(value)
                notifyDataSetChanged()
                return
            }

            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].id == value[newItemPosition].id
                }

                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].photo == value[newItemPosition].photo &&
                            field[oldItemPosition].updated == value[newItemPosition].updated
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        return PeopleViewHolder(LayoutInflater.from(parent.context).inflate(
                if (small) R.layout.person_item_small else R.layout.person_item, parent, false))
    }

    override fun getItemCount() = people.size

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        val person = people[position]
        val isMe = person.id == on<PersistenceHandler>().phoneId

        holder.itemView.name?.text = on<NameHandler>().getName(person)
        holder.itemView.active?.text = if (isMe) "Add to\nStory" else on<TimeStr>().tiny(person.updated)
        holder.itemView.name?.visible = !isMe
        holder.itemView.activeNowIndicator.visible = !isMe && on<TimeAgo>().fifteenMinutesAgo().before(person.updated)

        if (person.photo == null) {
            holder.itemView.photo.setImageResource(R.drawable.ic_person_black_24dp)
            holder.itemView.photo.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } else {
            on<PhotoHelper>().loadCircle(holder.itemView.photo, "${person.photo}?s=256")
            holder.itemView.photo.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        

        if (isMe) {

        } else {
            holder.itemView.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForPhone(holder.itemView, person.id!!)
            }

            holder.itemView.photo.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForPhone(holder.itemView, person.id!!)
            }
        }

        holder.itemView.addIndicator?.visible = isMe
    }
}

class PeopleViewHolder(view: View) : RecyclerView.ViewHolder(view)