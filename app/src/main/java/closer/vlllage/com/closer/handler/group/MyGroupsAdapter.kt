package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On

class MyGroupsAdapter(on: On) : PoolRecyclerAdapter<MyGroupsAdapter.MyGroupViewHolder>(on) {

    private var actions: List<GroupActionBarButton> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGroupViewHolder {
        return MyGroupViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.group_item, parent, false))
    }

    override fun onBindViewHolder(holder: MyGroupViewHolder, position: Int) {
        val groupName = holder.itemView.findViewById<TextView>(R.id.groupName)

        val actionBarButton = actions[position]
        groupName.setBackgroundResource(actionBarButton.backgroundDrawableRes)
        groupName.setCompoundDrawablesRelativeWithIntrinsicBounds(actionBarButton.icon, 0, 0, 0)
        groupName.compoundDrawablePadding = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
        groupName.compoundDrawableTintList = ColorStateList.valueOf(
                on<ResourcesHandler>().resources.getColor(actionBarButton.textColorRes, on<ActivityHandler>().activity!!.theme)
        )
        groupName.text = actionBarButton.name!!
        groupName.setTextColor(on<ResourcesHandler>().resources.getColor(actionBarButton.textColorRes))
        groupName.setOnClickListener(actionBarButton.onClick!!)
        groupName.setOnLongClickListener { view ->
            if (actionBarButton.onLongClick != null) {
                actionBarButton.onLongClick!!.onClick(view)
                return@setOnLongClickListener true
            }

            false
        }
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    fun setActions(actions: List<GroupActionBarButton>) {
        this.actions = actions
        notifyDataSetChanged()
    }

    inner class MyGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
