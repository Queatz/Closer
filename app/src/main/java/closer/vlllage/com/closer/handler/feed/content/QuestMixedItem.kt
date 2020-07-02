package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import closer.vlllage.com.closer.store.models.Quest
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.item_quest.view.*
import kotlin.random.Random

class QuestMixedItem(val quest: Quest) : MixedItem(MixedItemType.Quest)

class QuestViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Quest) {
    lateinit var on: On
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
    }

    private fun bindQuest(holder: QuestViewHolder, quest: Quest) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<GroupActionGridRecyclerViewHandler>()
        }

        holder.on<GroupActionGridRecyclerViewHandler>().attach(holder.itemView.groupActionsRecyclerView, GroupActionDisplay.Layout.QUEST)

        holder.about.text = listOf(
                "Not started, finish by September 22nd, 2021 (in 6 months)",
                "In Progress, finish by May 7th, 2021 (in 1 week)",
                "Finished on October 6rd, 2021 (2 years ago)",
                "In Progress, finish by May 3rd, 2021 (in 22 days)",
                "Not started, no finish date"
        ).random()

        holder.about.setOnClickListener {
            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.quest_status), holder.about.text.toString())
        }

        holder.name.text = quest.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown)

        holder.name.setOnClickListener {
            // todo go to quest group
        }

        // todo quest group
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

        holder.on<GroupActionDisplay>().onGroupActionClickListener = { it, proceed ->
            if (true/* quest not started */) {
                on<AlertHandler>().make().apply {
                    title = "Start quest"
                    message = "Activities will contribute towards this quest."
                    positiveButton = "Start quest"
                    negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
                    positiveButtonCallback = {
                        on<ToastHandler>().show("You started questing!")
                        proceed()
                    }
                    negativeButtonCallback = { proceed() }
                    show()
                }
            }
        }

        holder.card.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = "Start this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = "Stop this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = "Restart this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = "Finish this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_group_black_24dp, title = "See people who did this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, title = "Open group") {}
            )
        }
    }
}