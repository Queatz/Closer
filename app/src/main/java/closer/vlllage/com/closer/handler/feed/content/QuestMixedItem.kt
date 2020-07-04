package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.quest.QuestHandler
import closer.vlllage.com.closer.handler.quest.QuestProgressAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.item_quest.view.*
import kotlin.random.Random

class QuestMixedItem(val quest: Quest) : MixedItem(MixedItemType.Quest)

class QuestViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Quest) {
    lateinit var on: On
    var progress: List<QuestProgress> = listOf()
    var progressByMe: QuestProgress? = null
    var activeProgress: QuestProgress? = null
    lateinit var questProgressAdapter: QuestProgressAdapter
    val about = itemView.about!!
    val name = itemView.name!!
    val card = itemView.card!!
}

class QuestMixedItemAdapter(private val on: On) : MixedItemAdapter<QuestMixedItem, QuestViewHolder> {
    override fun bind(holder: QuestViewHolder, item: QuestMixedItem, position: Int) {
        bindQuest(holder, item.quest)
    }

    override fun getMixedItemClass() = QuestMixedItem::class
    override fun getMixedItemType() = MixedItemType.Quest
    override fun areItemsTheSame(old: QuestMixedItem, new: QuestMixedItem) = old.quest.objectBoxId == new.quest.objectBoxId
    override fun areContentsTheSame(old: QuestMixedItem, new: QuestMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = QuestViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quest, parent, false))

    override fun onViewRecycled(holder: QuestViewHolder) {
        holder.on.off()
        holder.progress = listOf()
        holder.progressByMe = null
        holder.activeProgress = null
    }

    private fun bindQuest(holder: QuestViewHolder, quest: Quest) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<QuestHandler>()
            use<GroupActionGridRecyclerViewHandler>()
        }

        holder.questProgressAdapter = QuestProgressAdapter(holder.on) { it, view ->
            if (holder.activeProgress == it) {
                on<GroupActivityTransitionHandler>().showGroupMessages(view, it.groupId)
            } else {
                holder.activeProgress = it
                refreshProgress(holder, quest)
            }
        }

        holder.itemView.peopleRecyclerView.adapter = holder.questProgressAdapter
        holder.itemView.peopleRecyclerView.layoutManager = LinearLayoutManager(
                holder.itemView.peopleRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        holder.on<QuestHandler>().questProgress(quest) {
            holder.progress = it
            holder.questProgressAdapter.questProgresses = it.toMutableList()

            holder.progressByMe = holder.progress.find { it.ofId == on<PersistenceHandler>().phoneId }

            if (holder.activeProgress == null) {
                holder.activeProgress = holder.progressByMe
            }

            refreshProgress(holder, quest)
        }

        holder.on<GroupActionGridRecyclerViewHandler>().attach(holder.itemView.groupActionsRecyclerView, GroupActionDisplay.Layout.QUEST)

        holder.about.setOnClickListener {
            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.quest_status), holder.about.text.toString())
        }

        holder.name.text = quest.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown)

        holder.name.setOnClickListener {
            on<QuestHandler>().openQuest(quest)
        }

        // todo load quest group
        on<GroupScopeHandler>().setup(Group().apply { isPublic = Random.nextBoolean() }, holder.itemView.scopeIndicatorButton)

        holder.on<LightDarkHandler>().onLightChanged.subscribe {
            holder.name.setTextColor(it.text)
            holder.about.setTextColor(it.text)
            holder.name.setBackgroundResource(it.clickableRoundedBackground8dp)
            holder.about.setBackgroundResource(it.clickableRoundedBackground8dp)
            holder.itemView.optionsButton.setTextColor(on<ResourcesHandler>().resources.getColor(when (it.light) {
                true -> R.color.forestgreen
                false -> R.color.text
            }))
            holder.itemView.scopeIndicatorButton.imageTintList = it.tint
            holder.itemView.goToGroup.imageTintList = it.tint
            holder.card.setBackgroundResource(when (it.light) {
                true -> R.drawable.clickable_white_rounded_12dp
                false -> R.drawable.clickable_forestgreen_rounded_12dp
            })
        }.also {
            holder.on<DisposableHandler>().add(it)
        }

        holder.on<GroupActionDisplay>().questActionConfigProvider = { groupAction ->
            quest.flow?.items?.firstOrNull { it.groupActionId == groupAction.id!! }?.also {
                it.current = holder.activeProgress?.progress?.items?.get(groupAction.id!!)?.current ?: 0
            }
        }

        holder.on<GroupActionDisplay>().onGroupActionClickListener = { it, proceed ->
            if (holder.progressByMe == null) {
                on<QuestHandler>().startQuest(quest) { questProgress ->
                    if (questProgress != null) {
                        on<QuestHandler>().addProgress(quest, questProgress, it) { proceed() }
                    } else {
                        proceed()
                    }
                }
            } else {
                on<QuestHandler>().addProgress(quest, holder.progressByMe!!, it) { proceed() }
            }
        }

        holder.card.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.start_quest)) {
                        on<QuestHandler>().startQuest(quest) {}
                    },
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.stop_quest)) {
                        on<QuestHandler>().stopQuest(holder.progressByMe!!) {}
                    },
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.resume_quest)) {
                        on<QuestHandler>().resumeQuest(holder.progressByMe!!) {}
                    },
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.finish_quest)) {
                        on<QuestHandler>().finishQuest(holder.progressByMe!!) {}
                    },
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.open_group)) {}
            )
        }
    }

    private fun refreshProgress(holder: QuestViewHolder, quest: Quest) {
        holder.questProgressAdapter.active = holder.activeProgress

        on<StoreHandler>().store.box(GroupAction::class).query(GroupAction_.id.oneOf(quest.flow!!.items.map { it.groupActionId!! }.toTypedArray()))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groupActions ->
                    holder.on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(groupActions, true)
                }.also {
                    holder.on<DisposableHandler>().add(it)
                }

        holder.about.text = when {
            holder.activeProgress?.finished != null -> on<ResourcesHandler>().resources.getString(R.string.finished_x,
                    on<TimeStr>().prettyDate(holder.activeProgress!!.finished!!))
            holder.activeProgress?.active == true -> on<ResourcesHandler>().resources.getString(R.string.in_progress_x,
                    on<QuestHandler>().questFinishText(quest, holder.activeProgress!!.created))
            holder.activeProgress?.stopped != null -> on<ResourcesHandler>().resources.getString(R.string.stopped_x,
                    on<TimeStr>().prettyDate(holder.activeProgress!!.stopped!!))
            else -> on<ResourcesHandler>().resources.getString(R.string.not_started_x,
                    on<QuestHandler>().questFinishText(quest))
        }
    }
}