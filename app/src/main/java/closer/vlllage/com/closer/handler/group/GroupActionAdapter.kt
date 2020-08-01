package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject


class GroupActionAdapter(on: On,
                         layout: GroupActionDisplay.Layout,
                         onGroupActionClickListener: GroupActionClickListener? = null)
    : PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder>(on) {

    init {
        on<GroupActionDisplay>().onGroupActionClickListener = onGroupActionClickListener
    }

    var scale = 1f
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var layout: GroupActionDisplay.Layout = layout
        set(value) {
            if (field == value) return

            field = value

            notifyDataSetChanged()
        }

    val onItemsChanged = BehaviorSubject.create<List<GroupAction>>()

    private val groupActions = mutableListOf<GroupAction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GroupActionViewHolder(LayoutInflater.from(parent.context)
            .inflate(when (layout) {
                GroupActionDisplay.Layout.TEXT -> R.layout.group_action_item
                GroupActionDisplay.Layout.PHOTO -> R.layout.group_action_photo_item
                GroupActionDisplay.Layout.QUEST -> R.layout.group_action_quest_item
            }, parent, false)).also { it ->
        it.on = On(on).apply {
            use<DisposableHandler>()
            use<GroupActionDisplay>().also {
                it.onGroupActionClickListener = { groupAction, proceed ->
                    (on<GroupActionDisplay>().onGroupActionClickListener ?: it.fallbackGroupActionClickListener).invoke(groupAction, proceed)
                }
                it.questActionConfigProvider = { on<GroupActionDisplay>().questActionConfigProvider?.invoke(it) }
            }
        }
    }

    override fun onBindViewHolder(holder: GroupActionViewHolder, position: Int) {
        holder.on<GroupActionDisplay>().display(holder.itemView, groupActions[position], layout, scale = scale)
    }

    override fun onViewRecycled(holder: GroupActionViewHolder) {
        holder.on.off()
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int) = layout.ordinal

    override fun getItemCount() = groupActions.size

    fun setGroupActions(groupActions: List<GroupAction>, disableAnimation: Boolean = false) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = this@GroupActionAdapter.groupActions.size
            override fun getNewListSize() = groupActions.size

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@GroupActionAdapter.groupActions[oldPosition].id == groupActions[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@GroupActionAdapter.groupActions[oldPosition].name == groupActions[newPosition].name && this@GroupActionAdapter.groupActions[oldPosition].photo == groupActions[newPosition].photo
            }
        }, true)
        this.groupActions.clear()
        this.groupActions.addAll(groupActions)

        if (disableAnimation) notifyDataSetChanged()
        else diffResult.dispatchUpdatesTo(this)

        onItemsChanged.onNext(this.groupActions)
    }

    inner class GroupActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
    }
}
