package closer.vlllage.com.closer.handler.feed.content

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.QuestResult
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.quest.QuestDisplaySettings
import closer.vlllage.com.closer.handler.quest.QuestHandler
import closer.vlllage.com.closer.handler.quest.QuestLinkAdapter
import closer.vlllage.com.closer.handler.quest.QuestProgressAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import closer.vlllage.com.closer.store.models.Quest
import closer.vlllage.com.closer.store.models.QuestProgress
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.item_quest.view.*

class QuestMixedItem(val quest: Quest) : MixedItem(MixedItemType.Quest)

class QuestViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Quest) {
    lateinit var on: On
    var progress: List<QuestProgress> = listOf()
    var progressByMe: QuestProgress? = null
    var activeProgress: QuestProgress? = null
    lateinit var questProgressAdapter: QuestProgressAdapter
    lateinit var nextQuestsAdapter: QuestLinkAdapter
    val about = itemView.about!!
    val description = itemView.description!!
    val name = itemView.name!!
    val card = itemView.card!!
    val overallProgress = itemView.overallProgress!!
}

class QuestMixedItemAdapter(private val on: On) : MixedItemAdapter<QuestMixedItem, QuestViewHolder> {
    override fun bind(holder: QuestViewHolder, item: QuestMixedItem, position: Int) {
        bindQuest(holder, item.quest)
    }

    override fun getMixedItemClass() = QuestMixedItem::class
    override fun getMixedItemType() = MixedItemType.Quest
    override fun areItemsTheSame(old: QuestMixedItem, new: QuestMixedItem) = old.quest.id == new.quest.id
    override fun areContentsTheSame(old: QuestMixedItem, new: QuestMixedItem) = old.quest.name == new.quest.name

    override fun onCreateViewHolder(parent: ViewGroup) = QuestViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quest, parent, false))

    override fun onViewRecycled(holder: QuestViewHolder) {
        holder.on.off()
        holder.progress = listOf()
        holder.progressByMe = null
        holder.activeProgress = null
        holder.questProgressAdapter.questProgresses = mutableListOf()
        holder.itemView.peopleRecyclerView.adapter = null
    }

    private fun bindQuest(holder: QuestViewHolder, quest: Quest) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<QuestHandler>()
            use<GroupActionGridRecyclerViewHandler>()
            use<GroupActionDisplay>()
        }

        if (on<QuestDisplaySettings>().isAbout) {
            holder.itemView.background = null
            holder.itemView.name.visible = false
            holder.itemView.scopeIndicatorButton.visible = false
            holder.itemView.goToGroup.visible = false
            holder.itemView.card.elevation = 0f
        } else {
            on<GroupScopeHandler>().setup(quest, holder.itemView.scopeIndicatorButton)
        }

        on<RefreshHandler>().refreshQuestProgresses(quest.id!!)

        holder.questProgressAdapter = QuestProgressAdapter(holder.on) { it, view ->
            if (holder.activeProgress == it) {
                on<MenuHandler>().show(
                        MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.show_progress) {
                            holder.on<QuestHandler>().openGroupForQuestProgress(view, it)
                        },
                        MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.deselect) {
                            holder.activeProgress = null
                            refreshProgress(holder, quest)
                        }
                )
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

        holder.nextQuestsAdapter = QuestLinkAdapter(holder.on, {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.remove_quest_link)) {
                        on<ApiHandler>().removeQuestLink(quest.id!!, it.id!!).subscribe({
                            if (it.success) {
                                on<ToastHandler>().show(R.string.quest_unlinked)
                                loadLinks(holder, quest)
                            } else {
                                on<DefaultAlerts>().thatDidntWork()
                            }
                        }, {
                            on<DefaultAlerts>().thatDidntWork()
                        }).also {
                            on<DisposableHandler>().add(it)
                        }
                    }
            )
        }) { it, view ->
            on<GroupActivityTransitionHandler>().showGroupForQuest(view, it)
        }

        holder.itemView.nextQuestsRecyclerView.adapter = holder.nextQuestsAdapter
        holder.itemView.nextQuestsRecyclerView.layoutManager = LinearLayoutManager(
                holder.itemView.nextQuestsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        holder.itemView.nextQuestsHeader.visible = false
        holder.itemView.nextQuestsRecyclerViewContainer.visible = false

        loadLinks(holder, quest)
        loadQuestActions(quest)

        holder.description.visible = false

        if (quest.groupId != null) {
            holder.on<DataHandler>().getGroup(quest.groupId!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
                if (!on<QuestDisplaySettings>().isAbout) {
                    holder.description.visible = it.about.isNullOrEmpty().not()
                    holder.description.text = it.about ?: ""
                }
            }, {
                on<ConnectionErrorHandler>().notifyConnectionError()
            }).also {
                on<DisposableHandler>().add(it)
            }
        }

        holder.on<QuestHandler>().questProgress(quest) {
            holder.progress = it
            holder.questProgressAdapter.questProgresses = it.toMutableList()

            holder.progressByMe = holder.progress.find { it.ofId == on<PersistenceHandler>().phoneId }

            if (on<QuestDisplaySettings>().stageQuestProgressId != null) {
                holder.activeProgress = holder.progress.find { it.id == on<QuestDisplaySettings>().stageQuestProgressId }
            } else if (holder.activeProgress == null) {
                holder.activeProgress = holder.progressByMe
            } else {
                holder.activeProgress = holder.progress.find { it.id == holder.activeProgress?.id }
            }

            refreshProgress(holder, quest)
        }

        holder.on<GroupActionGridRecyclerViewHandler>().attach(holder.itemView.groupActionsRecyclerView, GroupActionDisplay.Layout.QUEST)

        refreshProgress(holder, quest)

        holder.about.setOnClickListener {
            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.quest_status), holder.about.text.toString())
        }

        holder.name.text = quest.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown)

        holder.name.setOnClickListener {
            on<QuestHandler>().openQuest(quest)
        }

        holder.on<LightDarkHandler>().onLightChanged.subscribe {
            holder.name.setTextColor(it.text)
            holder.about.setTextColor(it.text)
            holder.description.setTextColor(it.text)
            holder.itemView.nextQuestsHeader.setTextColor(it.hint)
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
            holder.overallProgress?.progressTintList = when (it.light) {
                true -> ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.forestgreen))
                false -> ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.colorAccent))
            }
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
                holder.on<QuestHandler>().startQuest(quest) { questProgress ->
                    if (questProgress != null) {
                        holder.on<QuestHandler>().addProgress(quest, questProgress, it) { proceed() }
                    } else {
                        proceed()
                    }
                }
            } else {
                holder.on<QuestHandler>().addProgress(quest, holder.progressByMe!!, it) { proceed() }
            }
        }

        holder.card.setOnClickListener {
            val me = on<PersistenceHandler>().phoneId
            val questProgress = if (holder.activeProgress?.ofId == me) holder.activeProgress else holder.progressByMe

            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.open_group)) {
                        holder.on<GroupActivityTransitionHandler>().showGroupMessages(null, quest.groupId)
                    },
                    MenuHandler.MenuOption(R.drawable.ic_baseline_play_arrow_24, title = on<ResourcesHandler>().resources.getString(R.string.start_quest)) {
                        holder.on<QuestHandler>().startQuest(quest) {}
                    }.visible(questProgress == null || !(questProgress.active ?: false)),
                    MenuHandler.MenuOption(R.drawable.ic_check_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.finish_quest)) {

                        holder.on<QuestHandler>().finishQuest(questProgress!!) {}
                    }.visible(questProgress?.let { it.finished == null && it.active == true } ?: false),
                    MenuHandler.MenuOption(R.drawable.ic_baseline_stop_24, title = on<ResourcesHandler>().resources.getString(R.string.stop_quest)) {
                        holder.on<QuestHandler>().stopQuest(questProgress!!) {}
                    }.visible(questProgress?.active ?: false),
                    MenuHandler.MenuOption(R.drawable.ic_baseline_play_arrow_24, title = on<ResourcesHandler>().resources.getString(R.string.resume_quest)) {
                        holder.on<QuestHandler>().resumeQuest(questProgress!!) {}
                    }.visible(questProgress?.let { it.finished == null && it.active?.let { !it } ?: false } ?: false),
                    MenuHandler.MenuOption(R.drawable.ic_add_black_24dp, title = on<ResourcesHandler>().resources.getString(R.string.add_next_quest)) {
                        on<QuestHandler>().addLinkedQuest(quest) { loadLinks(holder, quest) }
                    }
            )
        }
    }

    private fun loadQuestActions(quest: Quest) {
        on<RefreshHandler>().refreshQuestActions(quest.id!!)
    }

    private fun loadLinks(holder: QuestViewHolder, quest: Quest) {
        on<ApiHandler>().getQuestLinks(quest.id!!).subscribe({
            holder.nextQuestsAdapter.setQuests(it.map { QuestResult.from(it) }, true)
            holder.itemView.nextQuestsHeader.visible = it.isNotEmpty()
            holder.itemView.nextQuestsRecyclerViewContainer.visible = it.isNotEmpty()
        }, {
            // ignored
        }).also {
            holder.on<DisposableHandler>().add(it)
        }
    }

    private fun refreshProgress(holder: QuestViewHolder, quest: Quest) {
        holder.questProgressAdapter.active = holder.activeProgress

        holder.on<StoreHandler>().store.box(GroupAction::class).query(GroupAction_.id.oneOf(quest.flow!!.items.map { it.groupActionId!! }.toTypedArray()))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groupActions ->
                    holder.on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(groupActions, true)
                }.also {
                    holder.on<DisposableHandler>().add(it)
                }

        if (holder.activeProgress?.active == true && holder.activeProgress!!.created != null) {
            holder.overallProgress.visible = true
            holder.overallProgress.progress = on<QuestHandler>().questFinishPercent(quest, holder.activeProgress!!.created!!).toInt()
        } else {
            holder.overallProgress.visible = false
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