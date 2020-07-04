package closer.vlllage.com.closer.handler.quest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.store.models.QuestProgress
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.person_item.view.*
import java.util.*

class QuestProgressAdapter constructor(private val on: On, private val onClick: (QuestProgress, View) -> Unit) : RecyclerView.Adapter<QuestProgressViewHolder>() {

    var active: QuestProgress? = null
        set (value) {
            field = value
            field?.let { activeObservable.onNext(it) }
        }

    var questProgresses = mutableListOf<QuestProgress>()
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

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = false
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    private val activeObservable = BehaviorSubject.create<QuestProgress>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestProgressViewHolder {
        return QuestProgressViewHolder(on, LayoutInflater.from(parent.context).inflate(R.layout.person_item_small, parent, false))
    }

    override fun onViewRecycled(holder: QuestProgressViewHolder) {
        holder.disposableGroup.clear()
    }

    override fun getItemCount() = questProgresses.size

    override fun onBindViewHolder(holder: QuestProgressViewHolder, position: Int) {
        val questProgress = questProgresses[position]

        // todo only show if it's a person
        holder.itemView.activeNowIndicator.visible = on<TimeAgo>().fifteenMinutesAgo().before(questProgress.updated ?: Date(0))

        // todo load group or person photo
        if (true) {
            holder.itemView.photo.setImageResource(R.drawable.ic_person_black_24dp)
            holder.itemView.photo.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } else {
//            on<PhotoHelper>().loadCircle(holder.itemView.photo, "${person.photo}?s=256")
            holder.itemView.photo.scaleType = ImageView.ScaleType.CENTER_CROP
        }

        activeObservable.switchMap { active ->
            on<LightDarkHandler>().onLightChanged.map { Pair(active, it) }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            if (it.first == questProgress || it.first.id == questProgress.id) {
                holder.itemView.photo.foreground = on<ResourcesHandler>().resources.getDrawable(if (it.second.light)
                    R.drawable.outline_forestgreen_rounded
                else
                    R.drawable.outline_accent_rounded)
            } else {
                holder.itemView.photo.foreground = null
            }
        }.also {
            holder.disposableGroup.add(it)
        }

        holder.itemView.photo.setOnClickListener {
            onClick(questProgress, it)
        }
    }
}

class QuestProgressViewHolder(on: On, view: View) : RecyclerView.ViewHolder(view) {
    val disposableGroup = on<DisposableHandler>().group()
}