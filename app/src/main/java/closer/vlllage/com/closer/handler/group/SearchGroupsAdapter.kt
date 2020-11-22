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
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.quest.QuestHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*

open class SearchGroupsAdapter constructor(
        on: On,
        private var showCreateGroup: Boolean,
        private val onGroupClickListener: ((group: Group, view: View) -> Unit)?,
        private val onCreateGroupClickListener: ((groupName: String, isPublic: Boolean) -> Unit)?
) : PoolRecyclerAdapter<RecyclerView.ViewHolder>(on) {

    private var createGroupName = BehaviorSubject.createDefault("")
    private var createIsPublic = true
    private val groups = mutableListOf<Group>()
    private var actionText: String? = null
    @LayoutRes
    private var layoutResId = R.layout.search_groups_item
    @DrawableRes
    private var backgroundResId = R.drawable.clickable_light
    private var isSmall: Boolean = false
    private var isNoAnimation: Boolean = false
    var flat: Boolean = false
    var transparentBackground: Boolean = false

    private val createGroupCount get() = if (showCreateGroup) 1 else 0

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
                .inflate(layoutResId, parent, false).also {
                    if (transparentBackground) {
                        it.background = null
                    }
                })
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchGroupsViewHolder -> {
                holder.on = On()

                if (position >= itemCount - createGroupCount) {
                    holder.action.text = on<ResourcesHandler>().resources.getString(R.string.continue_text)
                    holder.about.text = on<ResourcesHandler>().resources.getString(if (createIsPublic) R.string.add_new_public_group else R.string.add_new_private_group)
                    holder.backgroundPhoto.visible = false
                    setShadows(holder, false)
                    holder.actionRecyclerView.visible = false
                    holder.cardView.setOnClickListener {
                        onCreateGroupClickListener?.invoke(createGroupName.value
                                ?: "", createIsPublic)
                    }
                    holder.cardView.setOnLongClickListener(null)
                    holder.cardView.setBackgroundResource(if (isSmall) backgroundResId else if (createIsPublic) R.drawable.clickable_green_8dp else R.drawable.clickable_blue_8dp)

                    createGroupName.observeOn(AndroidSchedulers.mainThread()).subscribe {
                        holder.name.text = if (it.isNullOrBlank()) "+" else it
                    }.also { holder.on<DisposableHandler>().add(it) }
                    return
                }

                val group = groups[position]

                holder.cardView.setBackgroundResource(if (isSmall)
                    backgroundResId else on<GroupColorHandler>().getColorClickable8dp(group))

                holder.cardView.clipToOutline = true

                on<GroupNameHelper>().loadName(group, holder.name) { it }

                val recentActivity = (group.updated ?: Date(0)).after(on<TimeAgo>().weeksAgo())

                if (!group.hasEvent() && !group.isPublic && !group.physical) {
                    holder.action.text = if (actionText != null) actionText else if (recentActivity) on<TimeStr>().active(group.updated) else on<ResourcesHandler>().resources.getString(R.string.open_group)
                    holder.about.text = on<ResourcesHandler>().resources.getString(R.string.private_group)
                } else if (group.physical) {
                    holder.action.text = if (actionText != null) actionText else if (recentActivity) on<TimeStr>().active(group.updated) else on<ResourcesHandler>().resources.getString(R.string.open_group)
                    holder.about.text = group.about
                } else if (group.hasEvent()) {
                    holder.action.text = if (actionText != null) actionText else if (recentActivity) on<TimeStr>().active(group.updated) else on<ResourcesHandler>().resources.getString(R.string.open_event)
                    val event = on<StoreHandler>().store.box(Event::class).query()
                            .equal(Event_.id, group.eventId!!)
                            .build()
                            .findFirst()
                    holder.about.text = if (event != null)
                        on<EventDetailsHandler>().formatEventDetails(event)
                    else
                        on<ResourcesHandler>().resources.getString(R.string.event)
                } else if (group.ofKind == "quest") {
                    holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_quest)
                    holder.about.text = on<StoreHandler>().store.box(Quest::class).query()
                            .equal(Quest_.id, group.ofId!!)
                            .build()
                            .findFirst()?.let { quest -> on<QuestHandler>().questProgressText(on<StoreHandler>().store.box(QuestProgress::class).query()
                                    .equal(QuestProgress_.ofId, on<PersistenceHandler>().phoneId!!)
                                    .equal(QuestProgress_.questId, group.ofId!!)
                                    .equal(QuestProgress_.active, true)
                                    .build()
                                    .findFirst(), quest) }
                            ?: on<ResourcesHandler>().resources.getString(R.string.quest)
                } else {
                    holder.about.text = group.about
                    holder.action.text = if (actionText != null) actionText else if (recentActivity) on<TimeStr>().active(group.updated) else on<ResourcesHandler>().resources.getString(R.string.open_group)
                }

                if (flat) {
                    holder.container?.elevation = 0f
                }

                holder.cardView.setOnClickListener { view ->
                    onGroupClickListener?.invoke(group, holder.itemView)
                }
                holder.cardView.setOnLongClickListener { view ->
                    on<GroupMemberHandler>().changeGroupSettings(group)
                    true
                }

                if (isSmall) {
                    holder.actionRecyclerView.visible = false
                } else {
                    holder.actionRecyclerView.visible = true
                    holder.on.use(on<ApplicationHandler>())
                    holder.on.use(on<ActivityHandler>())
                    holder.on.use(on<ApiHandler>())
                    holder.on.use(on<StoreHandler>())

                    var questProgress: QuestProgress? = null
                    var quest: Quest? = null

                    if (group.ofKind == "quest") {
                        questProgress = on<StoreHandler>().store.box(QuestProgress::class).query()
                                .equal(QuestProgress_.ofId, on<PersistenceHandler>().phoneId!!)
                                .equal(QuestProgress_.questId, group.ofId!!)
                                .equal(QuestProgress_.active, true)
                                .build()
                                .findFirst()

                        quest = on<StoreHandler>().store.box(Quest::class).query()
                                .equal(Quest_.id, group.ofId!!)
                                .build()
                                .findFirst()
                    }

                    holder.on<GroupActionRecyclerViewHandler>().attach(holder.actionRecyclerView, GroupActionDisplay.Layout.TEXT, when {
                        quest != null -> ({ it, proceed ->
                            if (questProgress == null) {
                                holder.on<QuestHandler>().startQuest(quest) { questProgress ->
                                    if (questProgress != null) {
                                        holder.on<QuestHandler>().addProgress(quest, questProgress, it) { proceed() }
                                    } else {
                                        proceed()
                                    }
                                }
                            } else {
                                holder.on<QuestHandler>().addProgress(quest, questProgress, it) { proceed() }
                            }
                        })
                        else -> null
                    })
                    holder.on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupAction::class).query().let {
                        if (group.ofKind == "quest") {
                            it.`in`(GroupAction_.id, quest?.flow?.items?.map { it.groupActionId!! }?.filter { groupActionId ->
                                        !on<QuestHandler>().isGroupActionProgressDone(quest, questProgress, groupActionId)
                                    }?.toTypedArray() ?: arrayOf())
                        } else {
                            it.equal(GroupAction_.group, group.id!!)
                        }
                    }
                            .build().subscribe().single()
                            .on(AndroidScheduler.mainThread())
                            .observer { groupActions ->
                                holder.on<GroupActionRecyclerViewHandler>().recyclerView!!.visible = groupActions.isNotEmpty()
                                holder.on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                            })

                    on<ImageHandler>().get().clear(holder.backgroundPhoto)
                    if (group.photo != null) {
                        holder.backgroundPhoto.visible = true
                        holder.backgroundPhoto.setImageDrawable(null)
                        on<PhotoLoader>().softLoad(group.photo!!, holder.backgroundPhoto)
                    } else {
                        holder.backgroundPhoto.visible = false
                    }

                    setShadows(holder, group.photo != null)
                }

                if (!isSmall) {
                    if (group.hasPhone() && group.photo == null) {
                        holder.on<LightDarkHandler>().setLight(true)
                    } else {
                        holder.on<LightDarkHandler>().setLight(false)
                    }

                    holder.on<LightDarkHandler>().onLightChanged.observeOn(AndroidSchedulers.mainThread()).subscribe {
                        holder.name.setTextColor(it.text)
                        holder.about.setTextColor(it.text)
                        holder.action.setTextColor(it.text)
                    }
                }
            }
        }
    }

    private fun setShadows(holder: SearchGroupsViewHolder, show: Boolean) {
        listOf(holder.name, holder.about, holder.action).forEach {
            it.setShadowLayer(
                    it.shadowRadius,
                    it.shadowDx,
                    it.shadowDy,
                    on<ResourcesHandler>().resources.getColor(if (show) R.color.black else R.color.black_transparent)
            )
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

    fun setCreatePublicGroupName(createPublicGroupName: String) {
        this.createGroupName.onNext(createPublicGroupName)
    }

    fun setCreateIsPublic(isPublic: Boolean) {
        this.createIsPublic = isPublic
        notifyDataSetChanged()
    }

    fun showCreateOption(showCreate: Boolean) {
        if (showCreateGroup == showCreate) return
        showCreateGroup = showCreate
        notifyDataSetChanged()
    }

    fun setGroups(groups: List<Group>) {
        if (isNoAnimation || this.groups.isEmpty()) {
            this.groups.clear()
            this.groups.addAll(groups)
            notifyDataSetChanged()
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = this@SearchGroupsAdapter.groups.size

            override fun getNewListSize() = groups.size

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@SearchGroupsAdapter.groups[oldPosition].id == groups[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@SearchGroupsAdapter.groups[oldPosition].name == groups[newPosition].name &&
                        this@SearchGroupsAdapter.groups[oldPosition].photo == groups[newPosition].photo
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
        var container = itemView.findViewById<View>(R.id.container)
        var cardView: View = itemView.findViewById(R.id.rootView)
        var name: TextView = itemView.findViewById(R.id.name)
        var about: TextView = itemView.findViewById(R.id.about)
        var action: TextView = itemView.findViewById(R.id.action)
        var actionRecyclerView: RecyclerView = itemView.findViewById(R.id.actionRecyclerView)
        var backgroundPhoto: ImageView = itemView.findViewById(R.id.backgroundPhoto)
        lateinit var on: On
    }
}
