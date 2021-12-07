package closer.vlllage.com.closer.handler.feed

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.PersonItemBinding
import closer.vlllage.com.closer.databinding.PersonItemSmallBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.handler.helpers.TimeStr
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.story.StoryHandler
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On

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
        return PeopleViewHolder(when (small) {
            true -> PersonItemSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            false -> PersonItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        })
    }

    override fun getItemCount() = people.size

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        val person = people[position]
        val isMe = person.id == on<PersistenceHandler>().phoneId

        val name = (holder.binding as? PersonItemBinding)?.name
        val active = (holder.binding as? PersonItemBinding)?.active
        val addIndicator = (holder.binding as? PersonItemBinding)?.addIndicator

        val photo = when (holder.binding) {
            is PersonItemBinding -> holder.binding.photo
            is PersonItemSmallBinding -> holder.binding.photo
            else -> null
        }!!

        val activeNowIndicator = when (holder.binding) {
            is PersonItemBinding -> holder.binding.activeNowIndicator
            is PersonItemSmallBinding -> holder.binding.activeNowIndicator
            else -> null
        }!!

        name?.text = on<NameHandler>().getName(person)
        active?.text = if (isMe) "Add to\nStory" else on<TimeStr>().tiny(person.updated)
        name?.visible = !isMe
        activeNowIndicator.visible = !isMe && on<TimeAgo>().fifteenMinutesAgo().before(person.updated)

        if (person.photo == null) {
            photo.setImageResource(R.drawable.ic_person_black_24dp)
            photo.imageTintList = ColorStateList.valueOf(on<PhotoHelper>().colorForPhone(person.id!!))
            photo.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } else {
            on<PhotoHelper>().loadCircle(photo, "${person.photo}?s=256")
            photo.imageTintList = null
            photo.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        

        if (isMe) {
            holder.itemView.setOnClickListener {
                on<StoryHandler>().addToStory()
            }

            photo.setOnClickListener {
                on<StoryHandler>().addToStory()
            }
        } else {
            holder.itemView.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForPhone(holder.itemView, person.id!!)
            }

            photo.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForPhone(holder.itemView, person.id!!)
            }
        }

        addIndicator?.visible = isMe
    }
}

class PeopleViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)