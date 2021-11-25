package closer.vlllage.com.closer.handler.quest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.PersonItemSmallBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.QuestProgress
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*

class QuestProgressAdapter constructor(private val on: On, private val onClick: (QuestProgress, View) -> Unit) : RecyclerView.Adapter<QuestProgressViewHolder>() {

    var active: QuestProgress? = null
        set (value) {
            field = value
            activeObservable.onNext(field ?: QuestProgress())
        }

    var questProgresses = mutableListOf<QuestProgress>()
        set(value) {
            if (field.isEmpty()) {
                field.addAll(value)
                notifyDataSetChanged()
                return
            }

            if (value.isEmpty()) {
                field.clear()
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
        return QuestProgressViewHolder(on, PersonItemSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onViewRecycled(holder: QuestProgressViewHolder) {
        holder.disposableGroup.clear()
    }

    override fun getItemCount() = questProgresses.size

    override fun onBindViewHolder(holder: QuestProgressViewHolder, position: Int) {
        val questProgress = questProgresses[position]

        holder.binding.activeNowIndicator.visible = false
        holder.binding.status.visible = questProgress.active?.not() ?: false
        holder.binding.status.setImageResource(when {
            questProgress.stopped != null -> R.drawable.ic_baseline_stop_24
            else -> R.drawable.ic_check_black_24dp
        })

        // todo don't make call if ofId is a Group
        holder.binding.photo.setImageResource(R.drawable.ic_person_black_24dp)
        holder.binding.photo.scaleType = ImageView.ScaleType.CENTER_INSIDE

        on<DataHandler>().getPhone(questProgress.ofId!!).subscribe({
            holder.binding.activeNowIndicator.visible = on<TimeAgo>().fifteenMinutesAgo().before(it.updated
                    ?: Date(0))

            if (it.photo.isNullOrBlank()) {
                holder.binding.photo.setImageResource(R.drawable.ic_person_black_24dp)
                holder.binding.photo.scaleType = ImageView.ScaleType.CENTER_INSIDE
            } else {
                holder.binding.photo.scaleType = ImageView.ScaleType.CENTER_CROP
                on<PhotoHelper>().loadCircle(holder.binding.photo, "${it.photo}?s=256")
            }
        }, {}).also {
            holder.disposableGroup.add(it)
        }

        activeObservable.switchMap { active ->
            on<LightDarkHandler>().onLightChanged.map { Pair(active, it) }
        }.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            listOf(holder.binding.photo, holder.binding.status).forEach { view ->
                if (it.first == questProgress || it.first.id == questProgress.id) {
                    view.foreground = on<ResourcesHandler>().resources.getDrawable(if (it.second.light)
                        R.drawable.outline_forestgreen_rounded
                    else
                        R.drawable.outline_accent_rounded)
                } else {
                    view.foreground = null
                }
            }
        }.also {
            holder.disposableGroup.add(it)
        }

        holder.binding.photo.setOnClickListener {
            onClick(questProgress, it)
        }
    }
}

class QuestProgressViewHolder(on: On, val binding: PersonItemSmallBinding) : RecyclerView.ViewHolder(binding.root) {
    val disposableGroup = on<DisposableHandler>().group()
}