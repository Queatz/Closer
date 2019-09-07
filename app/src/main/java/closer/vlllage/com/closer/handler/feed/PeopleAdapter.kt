package closer.vlllage.com.closer.handler.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import kotlinx.android.synthetic.main.person_item.view.*

class PeopleAdapter(private val on: On) : RecyclerView.Adapter<PeopleViewHolder>() {

    var people = mutableListOf<Phone>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].id == value[newItemPosition].id
                }

                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].photo == value[newItemPosition].photo
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        return PeopleViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.person_item, parent, false))
    }

    override fun getItemCount() = people.size

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        val person = people[position]

        holder.itemView.name.text = on<NameHandler>().getName(person)

        if (person.photo == null) {
            holder.itemView.photo.setImageResource(R.drawable.ic_person_black_24dp)
        } else {
            on<PhotoHelper>().loadCircle(holder.itemView.photo, person.photo!!)
        }

        holder.itemView.photo.setOnClickListener {
            on<GroupActivityTransitionHandler>().showGroupForPhone(holder.itemView, person.id!!)
        }
    }
}

class PeopleViewHolder(view: View) : RecyclerView.ViewHolder(view)