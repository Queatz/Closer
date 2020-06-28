package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.Quest
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.quest_item.view.*
import kotlin.random.Random

class QuestMixedItem(val quest: Quest) : MixedItem(MixedItemType.Quest)

class QuestViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Quest) {
    lateinit var on: On
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
            .inflate(R.layout.quest_item, parent, false))

    override fun onViewRecycled(holder: QuestViewHolder) {
        holder.on.off()
    }

    private fun bindQuest(holder: QuestViewHolder, quest: Quest) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<GroupActionGridRecyclerViewHandler>()
        }

        holder.on<GroupActionGridRecyclerViewHandler>().attach(holder.itemView.groupActionsRecyclerView, GroupActionDisplay.Layout.QUEST)

        holder.name.text = "Get abs"

        on<StoreHandler>().store.box(GroupAction::class).query()
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groupActions ->
                    val random = Random(holder.adapterPosition)
                    holder.on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(groupActions
                            .subList(random.nextInt(groupActions.size - 7), groupActions.size)
                            .take(random.nextInt(6) + 1), true)
                }.also {
                    holder.on<DisposableHandler>().add(it)
                }

        holder.card.setOnClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_star_black_24dp, title = "Start this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_group_black_24dp, title = "See people who did this quest") {},
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, title = "Open group") {}
            )
        }
    }
}