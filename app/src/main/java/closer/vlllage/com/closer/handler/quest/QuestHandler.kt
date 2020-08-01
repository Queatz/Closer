package closer.vlllage.com.closer.handler.quest

import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.QuestProgressResult
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.*
import closer.vlllage.com.closer.handler.group.GroupActionAdapter
import closer.vlllage.com.closer.handler.group.GroupActionDisplay
import closer.vlllage.com.closer.handler.group.GroupActionGridRecyclerViewHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButtonToggleGroup
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.add_progress_modal.view.*
import kotlinx.android.synthetic.main.create_post_select_group_action.view.actionRecyclerView
import kotlinx.android.synthetic.main.create_post_select_group_action.view.searchActivities
import kotlinx.android.synthetic.main.create_quest_modal.view.*
import kotlinx.android.synthetic.main.create_quest_modal.view.name
import kotlinx.android.synthetic.main.edit_quest_action_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_duration_modal.view.*
import kotlinx.android.synthetic.main.edit_quest_finish_date_modal.view.*
import kotlinx.android.synthetic.main.item_quest.view.*
import kotlinx.android.synthetic.main.link_quest_modal.view.*
import java.util.*

class QuestHandler(private val on: On) {

    fun questProgress(quest: Quest, single: Boolean = false, callback: (List<QuestProgress>) -> Unit) {
        on<StoreHandler>().store.box(QuestProgress::class).query(QuestProgress_.questId.equal(quest.id!!))
                .orderDesc(QuestProgress_.updated)
                .orderDesc(QuestProgress_.active)
                .sort(on<SortHandler>().sortQuestProgresses())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .let {
                    if (single) it.single()
                    else it
                }
                .observer {
                    callback(it)
                }.also {
                    on<DisposableHandler>().add(it)
                }
    }

    fun startQuest(quest: Quest, callback: (result: QuestProgress?) -> Unit) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.start_quest)
            message = on<ResourcesHandler>().resources.getString(R.string.start_quest_details)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.start_quest)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
            positiveButtonCallback = {
                val questProgress = on<StoreHandler>().create(QuestProgress::class.java)!!.apply {
                    questId = quest.id!!
                    isPublic = true
                    ofId = on<PersistenceHandler>().phoneId!!
                    active = true
                }

                // Initialize with 0s so we can search for QuestProgresses with this groupActionId easily
                quest.flow?.items?.forEach {
                    addProgressInternal(questProgress, it.groupActionId!!, 0, set = true, sync = false)
                }

                on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)
                on<SyncHandler>().sync(questProgress)

                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.start_quest_confirmation))
                callback(questProgress)
            }
            negativeButtonCallback = { callback(null) }
            show()
        }
    }

    fun resumeQuest(questProgress: QuestProgress, callback: () -> Unit) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.resume_quest)
            message = on<ResourcesHandler>().resources.getString(R.string.resume_quest_details)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.resume_quest)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
            positiveButtonCallback = {
                questProgress.finished = null
                questProgress.stopped = null
                questProgress.active = true

                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.resume_quest_confirmation))
                on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)
                updateQuestProgress(questProgress, active = true) {}
                callback()
            }
            negativeButtonCallback = { callback() }
            show()
        }
    }

    fun openQuest(quest: Quest) {
        on<GroupActivityTransitionHandler>().showGroupMessages(null, quest.groupId)
    }

    fun createQuest() {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_ForestGreen
            title = on<ResourcesHandler>().resources.getString(R.string.create_a_quest)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.create_quest)
            layoutResId = R.layout.create_quest_modal
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = CreateQuestViewHolder(view).also {
                    it.disposableGroup = on<DisposableHandler>().group()
                    it.on = On(on).apply {
                        use<GroupActionGridRecyclerViewHandler>()
                        use<GroupActionDisplay>()
                    }
                }

                viewHolder.on<LightDarkHandler>().setLight(true)

                viewHolder.on<GroupActionGridRecyclerViewHandler>().attach(view.questActionRecyclerView, GroupActionDisplay.Layout.QUEST)
                viewHolder.on<GroupActionDisplay>().onGroupActionClickListener = { it, _ ->
                    editQuestAction(viewHolder, it, viewHolder.activityConfig[it.id!!]
                            ?: QuestAction(groupActionId = it.id!!).also { questAction ->
                                viewHolder.activityConfig[it.id!!] = questAction
                            }) { refresh(viewHolder) }
                }
                viewHolder.on<GroupActionDisplay>().questActionConfigProvider = { viewHolder.activityConfig[it.id!!] }

                val adapter = GroupActionAdapter(On(viewHolder.on).apply { use<GroupActionDisplay>() }, GroupActionDisplay.Layout.PHOTO) { it, _ ->
                    viewHolder.activities.add(it)

                    val questAction = viewHolder.activityConfig[it.id!!]
                            ?: QuestAction(groupActionId = it.id!!).also { questAction ->
                                viewHolder.activityConfig[it.id!!] = questAction
                            }

                    editQuestAction(viewHolder, it, questAction) { refresh(viewHolder) }

                    refresh(viewHolder)
                }

                viewHolder.searchGroupsAdapter = adapter

                view.isPublicToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
                    updateToggleButtonWeights(group)
                    if (isChecked) {
                        viewHolder.isPublic = checkedId == R.id.publicToggleButton
                    }
                }

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
                saveQuest(viewHolder)
            }
            buttonClickCallback = {
                val viewHolder = alertResult as CreateQuestViewHolder

                when {
                    viewHolder.name.isBlank() -> {
                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.name_this_quest))
                        false
                    }
                    viewHolder.activities.isEmpty() -> {
                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.quests_need_one_activity))
                        false
                    }
                    else -> true
                }
            }
            show()
        }
    }

    fun finishQuest(questProgress: QuestProgress, callback: () -> Unit) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.finish_quest)
            message = on<ResourcesHandler>().resources.getString(R.string.finish_quest_details)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.finish_quest)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
            positiveButtonCallback = {
                questProgress.finished = Date()
                questProgress.active = false

                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.finished_quest_confirmation))

                on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)
                updateQuestProgress(questProgress, finished = questProgress.finished, active = questProgress.active) {}
                callback()
            }
            negativeButtonCallback = { callback() }
            show()
        }
    }

    fun stopQuest(questProgress: QuestProgress, callback: () -> Unit) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.stop_quest)
            message = on<ResourcesHandler>().resources.getString(R.string.stop_quest_details)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.stop_quest)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
            positiveButtonCallback = {
                questProgress.stopped = Date()
                questProgress.active = false

                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.stopped_quest_confirmation))

                on<StoreHandler>().store.box(QuestProgress::class).put(questProgress)
                updateQuestProgress(questProgress, stopped = questProgress.stopped, active = questProgress.active) {}
                callback()
            }
            negativeButtonCallback = { callback() }
            show()
        }
    }

    fun addProgress(questProgress: QuestProgress, groupAction: GroupAction, callback: (() -> Unit)? = null) {
        on<DataHandler>().getQuest(questProgress.questId!!).subscribe({
            addProgress(it, questProgress, groupAction, callback)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }).also {
            on<DisposableHandler>().add(it)
        }
    }

    fun addProgress(quest: Quest, questProgress: QuestProgress, groupAction: GroupAction, callback: (() -> Unit)? = null) {
        quest.flow?.items?.firstOrNull { it.groupActionId == groupAction.id }?.let {
            val current = questProgress.progress?.items?.get(groupAction.id!!)?.current ?: 0

            when (it.type) {
                QuestActionType.Percent -> {
                    on<AlertHandler>().make().apply {
                        title = quest.name
                        message = "${on<AccountHandler>().name} ${groupAction.intent}"
                        layoutResId = R.layout.add_progress_modal
                        onAfterViewCreated = { alertConfig, view ->
                            alertConfig.alertResult = current
                            view.progressSeekBar.progress = alertConfig.alertResult as Int
                            view.progressText.text = on<ResourcesHandler>().resources.getString(R.string.x_done, view.progressSeekBar.progress.toString())
                            view.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                                    alertConfig.alertResult = progress
                                    view.progressText.text = on<ResourcesHandler>().resources.getString(R.string.x_done, progress.toString())
                                }

                                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                            })
                        }
                        negativeButton = on<ResourcesHandler>().resources.getString(R.string.skip)
                        negativeButtonCallback = { callback?.invoke() }
                        positiveButton = on<ResourcesHandler>().resources.getString(R.string.update_quest)
                        positiveButtonCallback = {
                            addProgressInternal(questProgress, groupAction.id!!, it as Int, set = true)
                            callback?.invoke()
                        }
                        show()
                    }
                }
                else -> {
                    on<AlertHandler>().make().apply {
                        title = quest.name
                        message = "${on<AccountHandler>().name} ${groupAction.intent}\n\n${on<ResourcesHandler>().resources.getString(R.string.x_of_y_done, (current + 1).toString(), it.value.toString())}"
                        negativeButton = on<ResourcesHandler>().resources.getString(R.string.skip)
                        negativeButtonCallback = { callback?.invoke() }
                        positiveButton = on<ResourcesHandler>().resources.getString(R.string.update_quest)
                        positiveButtonCallback = {
                            addProgressInternal(questProgress, groupAction.id!!, 1)
                            callback?.invoke()
                        }
                        show()
                    }
                }
            }
        } ?: on<DefaultAlerts>().thatDidntWork()
    }

    fun questFinishText(quest: Quest, relativeToDate: Date? = null): String {
        val finish = quest.flow?.finish

        return when {
            finish == null -> on<ResourcesHandler>().resources.getString(R.string.no_finish_date)
            relativeToDate == null -> {
                when {
                    finish.date != null -> on<ResourcesHandler>().resources.getString(R.string.finish_by_x, "${on<TimeStr>().prettyDate(finish.date!!)} (${on<TimeStr>().pretty(finish.date!!)})")
                    else -> on<ResourcesHandler>().resources.getString(R.string.finish_in_x, durationText(finish))
                }
            }
            else -> {
                when {
                    finish.date != null -> on<ResourcesHandler>().resources.getString(R.string.finish_by_x, "${on<TimeStr>().prettyDate(finish.date!!)} (${on<TimeStr>().pretty(finish.date!!)})")
                    else -> {
                        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
                            time = relativeToDate

                            add(when (finish.unit) {
                                QuestDurationUnit.Month -> Calendar.MONTH
                                QuestDurationUnit.Week -> Calendar.WEEK_OF_YEAR
                                else -> Calendar.DAY_OF_MONTH
                            }, finish.duration!!)
                        }

                        on<ResourcesHandler>().resources.getString(R.string.finish_x,
                                "${on<TimeStr>().approx(calendar.time, preposition = true)} (${on<TimeStr>().prettyDate(calendar.time)})")
                    }
                }

            }
        }
    }

    fun groupActionQuestProgress(groupActionId: String, phoneId: String? = null, single: Boolean = false, callback: (List<QuestProgress>) -> Unit) {
        on<StoreHandler>().store.box(QuestProgress::class).query(QuestProgress_.active.equal(true).and(
                QuestProgress_.progress.contains("\"groupActionId\":\"$groupActionId\"")
        ).let {
            if (phoneId != null) it.and(QuestProgress_.ofId.equal(phoneId)) else it
        })
                .orderDesc(QuestProgress_.updated)
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .let {
                    if (single) it.single()
                    else it
                }
                .observer {
                    callback(it)
                }.also {
                    on<DisposableHandler>().add(it)
                }
    }

    fun addLinkedQuest(quest: Quest, success: () -> Unit) {
        on<AlertHandler>().make().apply {
            val close = { dialog?.dismiss() }

            title = on<ResourcesHandler>().resources.getString(R.string.add_next_quest)
            message = on<ResourcesHandler>().resources.getString(R.string.add_next_quest_description)
            layoutResId = R.layout.link_quest_modal
            onAfterViewCreated = { alertConfig, view ->
                val holder = LinkQuestViewHolder(view).also {
                    it.disposableGroup = on<DisposableHandler>().group()
                }

                val adapter = QuestLinkAdapter(on) { it, _ -> addLinkedQuestInternal(quest, it) {
                    close()
                    success()
                } }

                view.questRecyclerView.adapter = adapter
                view.questRecyclerView.layoutManager = LinearLayoutManager(
                        view.questRecyclerView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )

                view.searchQuests.doOnTextChanged { text, _, _, _ ->
                    searchQuestsForQuest(quest, holder, adapter, text.toString())
                }

                searchQuestsForQuest(quest, holder, adapter)
            }
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.close)
            show()
        }
    }

    fun openGroupForQuestProgress(view: View?, questProgress: QuestProgress) {
        questProgress.groupId ?.let {
            on<GroupActivityTransitionHandler>().showGroupMessages(view, it)
        } ?: run {
            on<DefaultAlerts>().thatDidntWork()
        }
    }

    private fun addLinkedQuestInternal(quest: Quest, toQuest: Quest, success: () -> Unit) {
        on<ApiHandler>().addQuestLink(quest.id!!, toQuest.id!!).subscribe({
            if (it.success) {
                on<ToastHandler>().show(R.string.quest_linked)
                success()
            } else {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }).also {
            on<DisposableHandler>().add(it)
        }
    }

    private fun addProgressInternal(questProgress: QuestProgress, groupActionId: String, amount: Int, set: Boolean = false, sync: Boolean = true) {
        if (!questProgress.progress!!.items.containsKey(groupActionId)) {
            questProgress.progress!!.items[groupActionId] = QuestProgressAction().apply {
                this.groupActionId = groupActionId
                current = amount
            }
        } else {
            questProgress.progress!!.items[groupActionId]!!.current = if (set) amount else
                questProgress.progress!!.items[groupActionId]!!.current?.plus(amount)
                        ?: amount
        }

        if (sync) {
            updateQuestProgress(questProgress, progress = questProgress.progress) {
                on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.quest_updated))
            }
        }
    }

    private fun saveQuest(viewHolder: CreateQuestViewHolder) {
        on<MapHandler>().center?.let { latLng ->
            val quest = on<StoreHandler>().create(Quest::class.java)!!
            quest.name = viewHolder.name
            quest.isPublic = viewHolder.isPublic
            quest.latitude = latLng.latitude
            quest.longitude = latLng.longitude
            quest.flow = QuestFlow(
                    finish = viewHolder.finish,
                    items = viewHolder.activities.map {
                        QuestAction(
                                groupActionId = it.id,
                                type = viewHolder.activityConfig[it.id!!]!!.type,
                                value = viewHolder.activityConfig[it.id!!]!!.value,
                                current = viewHolder.activityConfig[it.id!!]!!.current
                        )
                    }
            )
            on<StoreHandler>().store.box(Quest::class).put(quest)
            on<SyncHandler>().sync(quest) {
                on<GroupActivityTransitionHandler>().showGroupForQuest(null, quest)
            }
        } ?: run {
            on<DefaultAlerts>().thatDidntWork()
        }
    }

    private fun refreshFinish(viewHolder: CreateQuestViewHolder) {
        viewHolder.view.finishDateText.visible = viewHolder.finish != null
        viewHolder.view.finishDateText.text = when {
            viewHolder.finish?.date != null -> on<TimeStr>().prettyDate(viewHolder.finish!!.date!!)
            viewHolder.finish?.duration != null -> durationText(viewHolder.finish!!)
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

    private fun durationText(finish: QuestFinish) = on<ResourcesHandler>().resources.getQuantityString(when (finish.unit) {
        QuestDurationUnit.Month -> R.plurals.date_approx_months
        QuestDurationUnit.Week -> R.plurals.date_approx_weeks
        else -> R.plurals.date_approx_days
    }, finish.duration ?: 1, finish.duration ?: 1)

    private fun updateToggleButtonWeights(group: MaterialButtonToggleGroup) {
        group.children.forEach {
            it.updateLayoutParams<LinearLayout.LayoutParams> {
                weight = if (it.id == group.checkedButtonId) 0f else 1f
            }
        }
    }

    private fun refresh(viewHolder: CreateQuestViewHolder) {
        searchGroupActivities(viewHolder, viewHolder.searchGroupsAdapter, viewHolder.view.searchActivities.text.toString())
        viewHolder.on<GroupActionGridRecyclerViewHandler>().adapter.setGroupActions(viewHolder.activities, true)
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

    private fun updateQuestProgress(questProgress: QuestProgress, finished: Date? = null, stopped: Date? = null, active: Boolean? = null, progress: QuestProgressFlow? = null, callback: (QuestProgress) -> Unit) {
        on<ApiHandler>().updateQuestProgress(questProgress.id!!, finished, stopped, active, progress)
                .observeOn(AndroidSchedulers.mainThread()).map {
                    QuestProgressResult.from(it)
                }.subscribe({
                    on<RefreshHandler>().refresh(it)
                    callback(it)
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }).also {
                    on<DisposableHandler>().add(it)
                }
    }

    private fun searchGroupActivities(holder: CreateQuestViewHolder, adapter: GroupActionAdapter, queryString: String? = null) {
        holder.disposableGroup.clear()

        on<Search>().groupActions(queryString = queryString) { groupActions ->
            adapter.setGroupActions(groupActions.filter { !holder.activityConfig.containsKey(it.id) })
        }.also { holder.disposableGroup.add(it) }
    }

    private fun searchQuestsForQuest(quest: Quest, holder: LinkQuestViewHolder, adapter: QuestLinkAdapter, queryString: String? = null) {
        holder.disposableGroup.clear()

        on<Search>().quests(LatLng(quest.latitude!!, quest.longitude!!), queryString = queryString) { quests ->
            adapter.setQuests(quests.filter { it.id != quest.id })
        }.also { holder.disposableGroup.add(it) }
    }

    private class CreateQuestViewHolder internal constructor(val view: View) {
        lateinit var on: On
        var isPublic: Boolean = false
        var finish: QuestFinish? = null
        var name = ""
        val activities = mutableListOf<GroupAction>()
        val activityConfig = mutableMapOf<String, QuestAction>()
        lateinit var searchGroupsAdapter: GroupActionAdapter
        lateinit var disposableGroup: DisposableGroup
    }

    private class LinkQuestViewHolder internal constructor(val view: View) {
        lateinit var disposableGroup: DisposableGroup
    }

    private class QuestActionViewHolder internal constructor(val view: View) {
        var times = 1
    }
}
