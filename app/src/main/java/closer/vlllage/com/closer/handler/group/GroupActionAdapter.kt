package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On
import java.util.*


class GroupActionAdapter(on: On,
                         private val layout: GroupActionDisplay.Layout)
    : PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder>(on) {

    private val groupActions = ArrayList<GroupAction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupActionViewHolder {
        return GroupActionViewHolder(LayoutInflater.from(parent.context)
                .inflate(if (layout == GroupActionDisplay.Layout.TEXT) R.layout.group_action_item else R.layout.group_action_photo_item, parent, false))
    }

    override fun onBindViewHolder(holder: GroupActionViewHolder, position: Int) {
        on<GroupActionDisplay>().display(holder.itemView, groupActions[position], layout)
    }

    override fun getItemCount() = groupActions.size

    fun setGroupActions(groupActions: List<GroupAction>) {
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
        diffResult.dispatchUpdatesTo(this)
    }

    inner class GroupActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

typealias OnGroupActionClickListener = (groupAction: GroupAction) -> Unit
typealias OnGroupActionLongClickListener = (groupAction: GroupAction) -> Unit
