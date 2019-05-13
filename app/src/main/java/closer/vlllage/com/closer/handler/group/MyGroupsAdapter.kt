package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.GroupColorHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Group

class MyGroupsAdapter(poolMember: PoolMember) : PoolRecyclerAdapter<MyGroupsAdapter.MyGroupViewHolder>(poolMember) {

    private var actions: List<GroupActionBarButton> = listOf()
    private var endActions: List<GroupActionBarButton> = listOf()
    private var groups: List<Group> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGroupViewHolder {
        return MyGroupViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.group_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyGroupViewHolder, position: Int) {
        var position = position
        val groupName = holder.itemView.findViewById<TextView>(R.id.groupName)

        if (position < actions.size) {
            val actionBarButton = actions[position]
            groupName.setBackgroundResource(actionBarButton.backgroundDrawableRes)
            groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(actionBarButton.icon, 0, 0, 0)
            groupName.compoundDrawablePadding = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.pad)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupName.compoundDrawableTintList = ColorStateList.valueOf(
                        `$`(ResourcesHandler::class.java).resources.getColor(android.R.color.white, `$`(ActivityHandler::class.java).activity!!.theme)
                )
            }
            groupName.text = actionBarButton.name!!
            groupName.setOnClickListener(actionBarButton.onClick!!)
            groupName.setOnLongClickListener { view ->
                if (actionBarButton.onLongClick != null) {
                    actionBarButton.onLongClick!!.onClick(view)
                    return@setOnLongClickListener true
                }

                false
            }
            return
        }

        position -= actions.size
        val isEndActionButton = position >= groups.size

        if (isEndActionButton) {
            val actionBarButton = endActions[position - groups.size]
            groupName.setBackgroundResource(actionBarButton.backgroundDrawableRes)
            groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(actionBarButton.icon, 0, 0, 0)
            groupName.compoundDrawablePadding = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.pad)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                groupName.compoundDrawableTintList = ColorStateList.valueOf(
                        `$`(ResourcesHandler::class.java).resources.getColor(android.R.color.white, `$`(ActivityHandler::class.java).activity!!.theme)
                )
            }
            groupName.text = actionBarButton.name
            groupName.setOnClickListener(actionBarButton.onClick)
            groupName.setOnLongClickListener { view ->
                if (actionBarButton.onLongClick != null) {
                    actionBarButton.onLongClick!!.onClick(view)
                    return@setOnLongClickListener true
                }

                false
            }
            return
        }

        val group = groups[position]

        groupName.setBackgroundResource(`$`(GroupColorHandler::class.java).getColorClickable(group))
        groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_black_18dp, 0, 0, 0)
        groupName.compoundDrawablePadding = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.pad)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            groupName.compoundDrawableTintList = ColorStateList.valueOf(
                    `$`(ResourcesHandler::class.java).resources.getColor(android.R.color.white, `$`(ActivityHandler::class.java).activity!!.theme)
            )
        }
        groupName.text = group.name

        groupName.setOnClickListener { view -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(holder.itemView, group.id) }

        groupName.setOnLongClickListener { view ->
            `$`(GroupMemberHandler::class.java).changeGroupSettings(group)

            true
        }
    }

    override fun getItemCount(): Int {
        return actions.size + groups.size + endActions.size
    }

    fun setGroups(groups: List<Group>) {
        this.groups = groups
        notifyDataSetChanged()
    }

    fun setActions(actions: List<GroupActionBarButton>) {
        this.actions = actions
        notifyDataSetChanged()
    }

    fun setEndActions(endActions: List<GroupActionBarButton>): MyGroupsAdapter {
        this.endActions = endActions
        notifyDataSetChanged()
        return this
    }

    inner class MyGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
