package closer.vlllage.com.closer.handler.quest

import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.GroupActionAdapter
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.GroupAction
import com.queatz.on.On
import kotlinx.android.synthetic.main.create_post_select_group_action.view.actionRecyclerView
import kotlinx.android.synthetic.main.create_post_select_group_action.view.searchActivities
import kotlinx.android.synthetic.main.create_quest_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_activity_modal.view.*

class QuestHandler(private val on: On) {
    fun createQuest() {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.create_a_quest)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.create_quest)
            layoutResId = R.layout.create_quest_modal
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = CreateQuestViewHolder(view).apply {
                    disposableGroup = on<DisposableHandler>().group()
                }

                on<LightDarkHandler>().setLight(true)

                on<GroupActionGridRecyclerViewHandler>().attach(view.questActionRecyclerView, GroupActionDisplay.Layout.QUEST)
                on<GroupActionDisplay>().onGroupActionClickListener = { it, _ ->
                    editQuestActivity(viewHolder, it)
                }

                val adapter = GroupActionAdapter(On(on).apply { use<GroupActionDisplay>() }, GroupActionDisplay.Layout.PHOTO) { it, _ ->
                    viewHolder.activities.add(it)
                    editQuestActivity(viewHolder, it)

                    refresh(viewHolder)
                }

                view.actionRecyclerView.adapter = adapter
                view.actionRecyclerView.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

                view.searchActivities.doOnTextChanged { text, _, _, _ ->
                    searchGroupActivities(viewHolder, adapter, text.toString())
                }

                searchGroupActivities(viewHolder, adapter, null)

                alertConfig.alertResult = viewHolder
            }
            buttonClickCallback = { alertResult ->
                val viewHolder = alertResult as CreateQuestViewHolder

                false
            }
            positiveButtonCallback = { alertResult ->
                val viewHolder = alertResult as CreateQuestViewHolder
            }
            show()
        }
    }

    private fun refresh(viewHolder: CreateQuestViewHolder) {
        on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(viewHolder.activities)
        viewHolder.view.questActionsHeader.visible = viewHolder.activities.isNotEmpty()
        viewHolder.view.questActionRecyclerView.visible = viewHolder.activities.isNotEmpty()
    }

    private fun editQuestActivity(viewHolder: CreateQuestViewHolder, groupAction: GroupAction) {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.edit_quest_activity)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.delete)
            layoutResId = R.layout.edit_quest_activity_modal
            negativeButtonCallback = {
                viewHolder.activities.remove(groupAction)
                refresh(viewHolder)
            }
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = QuestActivityViewHolder(view)

                view.numberOfTimes.doOnTextChanged { text, start, before, count ->
                    viewHolder.times = (text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)
                }

                view.interactionToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        when (checkedId) {
                            R.id.timesToggleButton -> {
                                view.numberOfTimes.visible = true
                            }
                            else -> {
                                view.numberOfTimes.visible = false
                            }
                        }
                    }
                }

                alertConfig.alertResult = viewHolder
            }
            show()
        }
    }

    private fun searchGroupActivities(holder: CreateQuestViewHolder, adapter: GroupActionAdapter, queryString: String?) {
        holder.disposableGroup.clear()

        on<Search>().groupActions(queryString = queryString) { groupActions ->
            adapter.setGroupActions(groupActions)
        }.also { holder.disposableGroup.add(it) }
    }

    private class CreateQuestViewHolder internal constructor(val view: View) {
        val activities = mutableListOf<GroupAction>()
        lateinit var disposableGroup: DisposableGroup
    }

    private class QuestActivityViewHolder internal constructor(val view: View) {
        var times = 1
    }
}