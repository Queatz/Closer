package closer.vlllage.com.closer.handler.quest

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
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
import com.google.android.material.button.MaterialButtonToggleGroup
import com.queatz.on.On
import kotlinx.android.synthetic.main.create_post_select_group_action.view.actionRecyclerView
import kotlinx.android.synthetic.main.create_post_select_group_action.view.searchActivities
import kotlinx.android.synthetic.main.create_quest_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_action_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_duration_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_finish_date_modal.view.*
import java.util.*

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
                    editQuestAction(viewHolder, it, viewHolder.activityConfig[it.id!!]
                            ?: QuestAction().also { questAction ->
                                viewHolder.activityConfig[it.id!!] = questAction
                            }) { refresh(viewHolder) } }
                on<GroupActionDisplay>().questActionConfigProvider = { viewHolder.activityConfig[it] }

                val adapter = GroupActionAdapter(On(on).apply { use<GroupActionDisplay>() }, GroupActionDisplay.Layout.PHOTO) { it, _ ->
                    viewHolder.activities.add(it)

                    val questAction = viewHolder.activityConfig[it.id!!] ?: QuestAction().also { questAction ->
                        viewHolder.activityConfig[it.id!!] = questAction
                    }

                    editQuestAction(viewHolder, it, questAction) { refresh(viewHolder) }

                    refresh(viewHolder)
                }

                viewHolder.searchGroupsAdapter = adapter

                view.isPublicToggle.addOnButtonCheckedListener { group, _, _ -> updateToggleButtonWeights(group) }
                view.finishDateToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    updateToggleButtonWeights(group)

                    if (isChecked) {
                        when (checkedId) {
                            R.id.durationToggleButton -> {
                                setDuration(viewHolder) { refreshFinish(viewHolder) }
                            }
                            R.id.specificToggleButton -> {
                                setFinishDate(viewHolder) { refreshFinish(viewHolder) }
                            }
                            else -> {
                                viewHolder.finish = null
                                refreshFinish(viewHolder)
                            }
                        }
                    }
                }
                updateToggleButtonWeights(view.finishDateToggle)
                updateToggleButtonWeights(view.isPublicToggle)

                view.actionRecyclerView.adapter = adapter
                view.actionRecyclerView.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

                view.searchActivities.doOnTextChanged { text, _, _, _ ->
                    searchGroupActivities(viewHolder, adapter, text.toString())
                }

                searchGroupActivities(viewHolder, adapter, null)

                view.name.doOnTextChanged { text, _, _, _ ->
                    viewHolder.name = text.toString().trim()
                }

                alertConfig.alertResult = viewHolder
            }
            positiveButtonCallback = { alertResult ->
                val viewHolder = alertResult as CreateQuestViewHolder

                
            }
            buttonClickCallback = {
                val viewHolder = alertResult as CreateQuestViewHolder

                when {
                    viewHolder.name.isBlank() -> {
                        on<DefaultAlerts>().message("Give this quest a name.")
                        false
                    }
                    viewHolder.activities.isEmpty() -> {
                        on<DefaultAlerts>().message("Quests must have at least 1 activity.")
                        false
                    }
                    else -> true
                }
            }
            show()
        }
    }

    private fun refreshFinish(viewHolder: CreateQuestViewHolder) {
        viewHolder.view.finishDateText.visible = viewHolder.finish != null
        viewHolder.view.finishDateText.text = when {
            viewHolder.finish?.date != null -> on<TimeStr>().prettyDate(viewHolder.finish!!.date!!)
            viewHolder.finish?.duration != null -> on<ResourcesHandler>().resources.getQuantityString(when (viewHolder.finish?.unit) {
                QuestDurationUnit.Month -> R.plurals.date_approx_months
                QuestDurationUnit.Week -> R.plurals.date_approx_weeks
                else -> R.plurals.date_approx_days
            }, viewHolder.finish?.duration ?: 1, viewHolder.finish?.duration ?: 1)
            else -> ""
        }
        viewHolder.view.finishDateText.setOnClickListener {
            when {
                viewHolder.finish?.date != null -> {
                    setFinishDate(viewHolder) { refreshFinish(viewHolder) }
                }
                viewHolder.finish?.duration != null -> {
                    setDuration(viewHolder) { refreshFinish(viewHolder) }
                }
            }
        }
    }

    private fun updateToggleButtonWeights(group: MaterialButtonToggleGroup) {
        group.children.forEach {
            it.updateLayoutParams<LinearLayout.LayoutParams> {
                weight = if (it.id == group.checkedButtonId) 0f else 1f
            }
        }
    }

    private fun refresh(viewHolder: CreateQuestViewHolder) {
        searchGroupActivities(viewHolder, viewHolder.searchGroupsAdapter, viewHolder.view.searchActivities.text.toString())
        on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(viewHolder.activities, true)
        viewHolder.view.questActionsHeader.visible = viewHolder.activities.isNotEmpty()
        viewHolder.view.questActionRecyclerView.visible = viewHolder.activities.isNotEmpty()
    }

    private fun setDuration(viewHolder: CreateQuestViewHolder, onChange: () -> Unit) {
        viewHolder.finish = QuestFinish(
                duration = viewHolder.finish?.duration ?: 1,
                unit = viewHolder.finish?.unit ?: QuestDurationUnit.Day
        )
        onChange()

        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.finish_in)
            layoutResId = R.layout.edit_quest_duration_modal
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.save)
            onAfterViewCreated = { _, view ->

                view.count.setText((viewHolder.finish?.duration ?: 1).toString())

                view.count.doOnTextChanged { text, start, before, count ->
                    viewHolder.finish = QuestFinish(
                            duration = (text.toString().toIntOrNull() ?: 1).coerceAtLeast(1),
                            unit = viewHolder.finish?.unit ?: QuestDurationUnit.Day
                    )
                    onChange()
                }

                view.intervalToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    updateToggleButtonWeights(group)

                    if (isChecked) {
                        view.count.setHint(when (checkedId) {
                            R.id.monthsToggleButton -> R.string.number_of_months
                            R.id.weeksToggleButton -> R.string.number_of_weeks
                            else -> R.string.number_of_days
                        })

                        when (checkedId) {
                            R.id.monthsToggleButton -> {
                                viewHolder.finish = QuestFinish(
                                        duration = (viewHolder.finish?.duration
                                                ?: 1).coerceAtLeast(1),
                                        unit = QuestDurationUnit.Month
                                )
                            }
                            R.id.weeksToggleButton -> {
                                viewHolder.finish = QuestFinish(
                                        duration = (viewHolder.finish?.duration
                                                ?: 1).coerceAtLeast(1),
                                        unit = QuestDurationUnit.Week
                                )
                            }
                            R.id.daysToggleButton -> {
                                viewHolder.finish = QuestFinish(
                                        duration = (viewHolder.finish?.duration
                                                ?: 1).coerceAtLeast(1),
                                        unit = QuestDurationUnit.Day
                                )
                            }
                        }

                        onChange()
                    }
                }

                view.intervalToggle.check(when (viewHolder.finish?.unit) {
                    QuestDurationUnit.Month -> R.id.monthsToggleButton
                    QuestDurationUnit.Week -> R.id.weeksToggleButton
                    else -> R.id.daysToggleButton
                })

                updateToggleButtonWeights(view.intervalToggle)
            }
            show()
        }
    }

    private fun setFinishDate(viewHolder: CreateQuestViewHolder, onChange: () -> Unit) {
        viewHolder.finish = QuestFinish(date = viewHolder.finish?.date ?: Date())
        onChange()

        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.finish_by)
            layoutResId = R.layout.edit_quest_finish_date_modal
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.save)
            onAfterViewCreated = { alertConfig, view ->
                val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
                    time = viewHolder.finish?.date ?: Date()
                }
                view.datePicker.minDate = Date().time
                view.datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    viewHolder.finish = QuestFinish(date = cal.time)
                    onChange()
                }
            }
            show()
        }
    }

    private fun editQuestAction(viewHolder: CreateQuestViewHolder, groupAction: GroupAction, questAction: QuestAction, onChange: () -> Unit) {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.edit_quest_activity)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.delete)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.save)
            layoutResId = R.layout.edit_quest_action_modal
            negativeButtonCallback = {
                viewHolder.activities.remove(groupAction)
                viewHolder.activityConfig.remove(groupAction.id!!)
                refresh(viewHolder)
            }
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = QuestActionViewHolder(view)

                view.numberOfTimes.doOnTextChanged { text, start, before, count ->
                    viewHolder.times = (text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)
                }

                view.interactionToggle.check(when (questAction.type) {
                    QuestActionType.Repeat -> R.id.timesToggleButton
                    QuestActionType.Percent -> R.id.percentToggleButton
                })

                view.numberOfTimes.setText(questAction.value.toString())
                view.numberOfTimes.visible = view.timesToggleButton.isChecked

                view.interactionToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    if (isChecked) {
                        view.numberOfTimes.visible = checkedId == R.id.timesToggleButton

                        questAction.type = when (checkedId) {
                            R.id.percentToggleButton -> QuestActionType.Percent
                            else -> QuestActionType.Repeat
                        }
                        onChange()
                    }

                    updateToggleButtonWeights(group)
                }
                updateToggleButtonWeights(view.interactionToggle)

                view.numberOfTimes.doOnTextChanged { text, start, before, count ->
                    questAction.value = (text.toString().toIntOrNull() ?: 1).coerceAtLeast(1)
                    onChange()
                }

                alertConfig.alertResult = viewHolder
            }
            show()
        }
    }

    private fun searchGroupActivities(holder: CreateQuestViewHolder, adapter: GroupActionAdapter, queryString: String?) {
        holder.disposableGroup.clear()

        on<Search>().groupActions(queryString = queryString) { groupActions ->
            adapter.setGroupActions(groupActions.filter { !holder.activityConfig.containsKey(it.id) })
        }.also { holder.disposableGroup.add(it) }
    }

    private class CreateQuestViewHolder internal constructor(val view: View) {
        var finish: QuestFinish? = null
        var name = ""
        val activities = mutableListOf<GroupAction>()
        val activityConfig = mutableMapOf<String, QuestAction>()
        lateinit var searchGroupsAdapter: GroupActionAdapter
        lateinit var disposableGroup: DisposableGroup
    }

    private class QuestActionViewHolder internal constructor(val view: View) {
        var times = 1
    }
}

enum class QuestActionType {
    Percent,
    Repeat
}

data class QuestFinish constructor(
        var date: Date? = null,
        var duration: Int? = null,
        var unit: QuestDurationUnit? = null
)

enum class QuestDurationUnit {
    Day,
    Week,
    Month
}

data class QuestAction constructor(
        var type: QuestActionType = QuestActionType.Repeat,
        var value: Int = 1,
        var current: Int = 0
)
