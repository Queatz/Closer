package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import java.util.*

open class SearchGroupsAdapter(on: On, private val onGroupClickListener: ((group: Group, view: View) -> Unit)?, private val onCreateGroupClickListener: ((groupName: String) -> Unit)?) : PoolRecyclerAdapter<RecyclerView.ViewHolder>(on) {

    private var createPublicGroupName: String? = null
    private val groups = ArrayList<Group>()
    private var actionText: String? = null
    @LayoutRes
    private var layoutResId = R.layout.search_groups_item
    @DrawableRes
    private var backgroundResId = R.drawable.clickable_light
    private var isSmall: Boolean = false
    private var isNoAnimation: Boolean = false

    private val createGroupCount: Int
        get() = if (createPublicGroupName == null) 0 else 1

    fun setLayoutResId(layoutResId: Int): SearchGroupsAdapter {
        this.layoutResId = layoutResId
        return this
    }

    fun setBackgroundResId(backgroundResId: Int): SearchGroupsAdapter {
        this.backgroundResId = backgroundResId
        return this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchGroupsViewHolder(LayoutInflater.from(parent.context)
                .inflate(layoutResId, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchGroupsViewHolder -> {
                if (position >= itemCount - createGroupCount) {
                    holder.on = On()
                    holder.action.text = on<ResourcesHandler>().resources.getString(R.string.create_group)
                    holder.name.text = createPublicGroupName
                    holder.about.text = on<ResourcesHandler>().resources.getString(R.string.add_new_public_group)
                    holder.backgroundPhoto.visibility = View.GONE
                    holder.actionRecyclerView.visibility = View.GONE
                    holder.cardView.setOnClickListener { view ->
                        onCreateGroupClickListener?.invoke(createPublicGroupName!!)
                    }
                    holder.cardView.setOnLongClickListener(null)
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else R.drawable.clickable_green_4dp)
                    return
                }

                val group = groups[position]

                holder.name.text = on<Val>().of(group.name!!, on<ResourcesHandler>().resources.getString(R.string.app_name))
                if (!group.hasEvent() && !group.isPublic) {
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else R.drawable.clickable_blue_4dp)
                    holder.action.text = if (actionText != null) actionText else on<ResourcesHandler>().resources.getString(R.string.open_group)
                    holder.about.text = on<ResourcesHandler>().resources.getString(R.string.private_group)
                } else if (group.physical) {
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else R.drawable.clickable_purple_4dp)
                    holder.action.text = if (actionText != null) actionText else on<ResourcesHandler>().resources.getString(R.string.open_group)
                    holder.about.text = on<Val>().of(group.about)
                } else if (group.hasEvent()) {
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else R.drawable.clickable_red_4dp)
                    holder.action.text = if (actionText != null) actionText else on<ResourcesHandler>().resources.getString(R.string.open_event)
                    val event = on<StoreHandler>().store.box(Event::class.java).query()
                            .equal(Event_.id, group.eventId!!)
                            .build().findFirst()
                    holder.about.text = if (event != null)
                        on<EventDetailsHandler>().formatEventDetails(event)
                    else
                        on<ResourcesHandler>().resources.getString(R.string.event)
                } else {
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else R.drawable.clickable_green_4dp)
                    holder.action.text = if (actionText != null) actionText else on<ResourcesHandler>().resources.getString(R.string.open_group)
                    holder.about.text = on<Val>().of(group.about)
                }
                holder.cardView.setOnClickListener { view ->
                    onGroupClickListener?.invoke(group, holder.itemView)
                }
                holder.cardView.setOnLongClickListener { view ->
                    on<GroupMemberHandler>().changeGroupSettings(group)
                    true
                }

                holder.on = On()

                if (isSmall) {
                    holder.actionRecyclerView.visibility = View.GONE
                } else {
                    holder.actionRecyclerView.visibility = View.VISIBLE
                    holder.on<ApplicationHandler>().app = on<ApplicationHandler>().app
                    holder.on<ActivityHandler>().activity = on<ActivityHandler>().activity
                    holder.on<ApiHandler>().setAuthorization(on<AccountHandler>().phone)
                    holder.on<GroupActionRecyclerViewHandler>().attach(holder.actionRecyclerView, GroupActionAdapter.Layout.TEXT)
                    holder.on<GroupActionRecyclerViewHandler>().onGroupActionRepliedListener = { groupAction ->
                        on<GroupActivityTransitionHandler>().showGroupMessages(holder.itemView, groupAction.group)
                    }
                    holder.on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupAction::class.java).query()
                            .equal(GroupAction_.group, group.id!!)
                            .build().subscribe().single()
                            .on(AndroidScheduler.mainThread())
                            .observer { groupActions ->
                                holder.on<GroupActionRecyclerViewHandler>().recyclerView!!.visibility = if (groupActions.isEmpty()) View.GONE else View.VISIBLE
                                holder.on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                            })

                    on<ImageHandler>().get().cancelRequest(holder.backgroundPhoto)
                    if (group.photo != null) {
                        holder.backgroundPhoto.visibility = View.VISIBLE
                        holder.backgroundPhoto.setImageDrawable(null)
                        on<PhotoLoader>().softLoad(group.photo!!, holder.backgroundPhoto)
                    } else {
                        holder.backgroundPhoto.visibility = View.GONE
                    }
                }

            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is SearchGroupsViewHolder -> {
                holder.on.off()
            }
        }
    }

    override fun getItemCount() = groups.size + createGroupCount

    fun setCreatePublicGroupName(createPublicGroupName: String?) {
        this.createPublicGroupName = createPublicGroupName
        notifyDataSetChanged()
    }

    fun setGroups(groups: List<Group>) {
        if (isNoAnimation) {
            this.groups.clear()
            this.groups.addAll(groups)
            this.notifyDataSetChanged()
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@SearchGroupsAdapter.groups.size
            }

            override fun getNewListSize(): Int {
                return groups.size
            }

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@SearchGroupsAdapter.groups[oldPosition].id != null &&
                        groups[newPosition].id != null &&
                        this@SearchGroupsAdapter.groups[oldPosition].id == groups[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@SearchGroupsAdapter.groups[oldPosition].name != null &&
                        groups[newPosition].name != null &&
                        this@SearchGroupsAdapter.groups[oldPosition].name == groups[newPosition].name
            }
        })
        this.groups.clear()
        this.groups.addAll(groups)
        diffResult.dispatchUpdatesTo(this)
    }

    fun setActionText(actionText: String) {
        this.actionText = actionText
    }

    fun setIsSmall(isSmall: Boolean) {
        this.isSmall = isSmall
    }

    fun setNoAnimation(noAnimation: Boolean): SearchGroupsAdapter {
        isNoAnimation = noAnimation
        return this
    }

    class SearchGroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardView: View = itemView.findViewById(R.id.rootView)
        var name: TextView = itemView.findViewById(R.id.name)
        var about: TextView = itemView.findViewById(R.id.about)
        var action: TextView = itemView.findViewById(R.id.action)
        var actionRecyclerView: RecyclerView = itemView.findViewById(R.id.actionRecyclerView)
        var backgroundPhoto: ImageView = itemView.findViewById(R.id.backgroundPhoto)
        lateinit var on: On
    }
}
