package closer.vlllage.com.closer.handler.phone

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.ItemGoalBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Goal
import closer.vlllage.com.closer.store.models.Goal_
import closer.vlllage.com.closer.store.models.Lifestyle
import closer.vlllage.com.closer.store.models.Lifestyle_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler

class GoalAdapter constructor(private val on: On, private val isLifestyleAdapter: Boolean, private val callback: (String) -> Unit) : RecyclerView.Adapter<GoalViewHolder>() {

    var name: String = on<ResourcesHandler>().resources.getString(R.string.them)
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    lateinit var type: String

    var isRemove: Boolean = false

    var items = mutableListOf<String>()
        set(value) {
            val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = field[oldItemPosition] == value[newItemPosition]
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size
                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
            })

            field.clear()
            field.addAll(value)

            diffUtil.dispatchUpdatesTo(this)
        }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        return GoalViewHolder(ItemGoalBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)).also {
            it.disposableGroup = on<DisposableHandler>().group()
        }
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.disposableGroup.clear()

        val item = items[position]

        holder.itemView.setOnClickListener {
            callback.invoke(item)
        }

        holder.binding.type.text = type
        holder.binding.goalName.text = item
        holder.binding.cheerButton.text = on<ResourcesHandler>().resources.getString(R.string.tap_for_options)
        holder.binding.count.visible = false

        if (isLifestyleAdapter.not()) {
            on<StoreHandler>().store.box(Goal::class).query(Goal_.name.equal(item)).build().subscribe().on(AndroidScheduler.mainThread()).observer {
                if (it.isEmpty()) return@observer

                val count = it.firstOrNull()?.phonesCount ?: 0

                holder.binding.count.visible = count > 1
                holder.binding.count.text = "$count"
            }.also { holder.disposableGroup.add(it) }
        } else {
            on<StoreHandler>().store.box(Lifestyle::class).query(Lifestyle_.name.equal(item)).build().subscribe().on(AndroidScheduler.mainThread()).observer {
                if (it.isEmpty()) return@observer

                val count = it.firstOrNull()?.phonesCount ?: 0

                holder.binding.count.visible = count > 1
                holder.binding.count.text = "$count"
            }.also { holder.disposableGroup.add(it) }
        }
    }
}

class GoalViewHolder(val binding: ItemGoalBinding) : RecyclerView.ViewHolder(binding.root) {
    lateinit var disposableGroup: DisposableGroup
}
