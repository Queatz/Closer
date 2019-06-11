package closer.vlllage.com.closer.handler.group

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ImageHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.Group_
import com.queatz.on.On
import java.util.*


class GroupActionAdapter(on: On,
                         private val layout: Layout,
                         private val onGroupActionClickListener: OnGroupActionClickListener?,
                         private val onGroupActionLongClickListener: OnGroupActionLongClickListener?)
    : PoolRecyclerAdapter<GroupActionAdapter.GroupActionViewHolder>(on) {

    private val groupActions = ArrayList<GroupAction>()

    enum class Layout {
        TEXT,
        PHOTO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupActionViewHolder {
        return GroupActionViewHolder(LayoutInflater.from(parent.context)
                .inflate(if (layout == Layout.TEXT) R.layout.group_action_item else R.layout.group_action_photo_item, parent, false))
    }

    override fun onBindViewHolder(holder: GroupActionViewHolder, position: Int) {
        val groupAction = groupActions[position]
        holder.actionName.text = groupActions[position].name

        val target: View

        when (layout) {
            Layout.PHOTO -> target = holder.itemView
            Layout.TEXT -> target = holder.actionName
            else -> target = holder.actionName
        }

        target.setOnClickListener {
            onGroupActionClickListener?.invoke(groupAction)
        }

        target.setOnLongClickListener {
            if (onGroupActionLongClickListener != null) {
                onGroupActionLongClickListener.invoke(groupAction)
                return@setOnLongClickListener true
            }

            false
        }

        if (layout == Layout.PHOTO) {
            val group = on<StoreHandler>().store.box(Group::class).query()
                    .equal(Group_.id, groupActions[position].group!!)
                    .build()
                    .findFirst()

            holder.groupName?.text = group?.name ?: ""

            when (getRandom(groupAction).nextInt(4)) {
                1 -> holder.itemView.setBackgroundResource(R.drawable.clickable_blue_8dp)
                2 -> holder.itemView.setBackgroundResource(R.drawable.clickable_accent_8dp)
                3 -> holder.itemView.setBackgroundResource(R.drawable.clickable_green_8dp)
                else -> holder.itemView.setBackgroundResource(R.drawable.clickable_red_8dp)
            }

            if (groupAction.photo != null) {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionSmallTextSize))
                holder.actionName.setBackgroundResource(R.color.black_25)
                holder.photo?.setImageDrawable(null)
                on<ImageHandler>().get().load(groupAction.photo!!.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=256")
                        .noPlaceholder()
                        .into(holder.photo)
            } else {
                holder.actionName.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.groupActionLargeTextSize))
                holder.actionName.background = null
                holder.photo?.setImageResource(getRandomBubbleBackgroundResource(groupAction))
            }
        }
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

    @DrawableRes
    private fun getRandomBubbleBackgroundResource(groupAction: GroupAction) = when (getRandom(groupAction).nextInt(3)) {
        0 -> R.drawable.bkg_bubbles
        1 -> R.drawable.bkg_bubbles_2
        else -> R.drawable.bkg_bubbles_3
    }

    private fun getRandom(groupAction: GroupAction): Random {
        return Random(if (groupAction.id == null)
            groupAction.objectBoxId
        else
            groupAction.id!!.hashCode().toLong())
    }

    inner class GroupActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var photo: ImageView? = itemView.findViewById(R.id.photo)
        var actionName: TextView = itemView.findViewById(R.id.actionName)
        var groupName: TextView? = itemView.findViewById(R.id.groupName)

        init {
            itemView.clipToOutline = true
        }
    }
}

typealias OnGroupActionClickListener = (groupAction: GroupAction) -> Unit
typealias OnGroupActionLongClickListener = (groupAction: GroupAction) -> Unit
