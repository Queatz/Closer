package closer.vlllage.com.closer.handler.event

import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.extensions.setToEndOfDay
import closer.vlllage.com.closer.extensions.setToStartOfDay
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.TaskHandler
import closer.vlllage.com.closer.handler.TaskType
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.ui.InterceptableScrollView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButtonToggleGroup
import com.queatz.on.On
import kotlinx.android.synthetic.main.post_event_modal.view.*
import kotlinx.android.synthetic.main.reminders.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class EventHandler constructor(private val on: On) {

    fun createNewEvent(latLng: LatLng,
                       isPublic: Boolean,
                       name: String? = null,
                       price: String? = null,
                       startsAt: Date? = null,
                       endsAt: Date? = null,
                       allDay: Boolean? = null,
                       onEventCreatedListener: OnEventCreatedListener) {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_Red
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.host_event)
            layoutResId = R.layout.post_event_modal
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = CreateEventViewHolder(view)

                val now = Calendar.getInstance(TimeZone.getDefault())

                val startCal = Calendar.getInstance(TimeZone.getDefault()).apply {
                    if (startsAt != null) {
                        time = startsAt
                    } else {
                        add(Calendar.HOUR_OF_DAY, 1)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }

                val endCal = Calendar.getInstance(TimeZone.getDefault()).apply {
                    if (endsAt != null) {
                        time = endsAt
                    } else {
                        add(Calendar.HOUR_OF_DAY, 4)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                }

                viewHolder.startsAtTimePicker.currentHour = startCal.get(Calendar.HOUR_OF_DAY)
                viewHolder.startsAtTimePicker.currentMinute = startCal.get(Calendar.MINUTE)
                viewHolder.endsAtTimePicker.currentHour = endCal.get(Calendar.HOUR_OF_DAY)
                viewHolder.endsAtTimePicker.currentMinute = endCal.get(Calendar.MINUTE)

                viewHolder.datePicker.minDate = now.timeInMillis
                viewHolder.isPublicToggle.check(if (isPublic) R.id.publicToggleButton else R.id.friendsToggleButton)

                view.isPublicToggle.addOnButtonCheckedListener { group, _, _ ->
                    on<ToggleHelper>().updateToggleButtonWeights(group)
                }

                on<ToggleHelper>().updateToggleButtonWeights(view.isPublicToggle)

                view.isAllDaySwitch.setOnCheckedChangeListener { _, isChecked ->
                    viewHolder.startsAtTimePicker.visible = !isChecked
                    viewHolder.endsAtTimePicker.visible = !isChecked
                }

                if (allDay == true) {
                    view.isAllDaySwitch.isChecked = true
                }

                name?.let { viewHolder.eventName.setText(it) }
                price?.let { viewHolder.eventPrice.setText(it) }

                viewHolder.datePicker.init(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)) { _, year, month, dayOfMonth ->
                    updateDateButtonText(viewHolder.dateTextView, year, month, dayOfMonth)
                    viewHolder.changeDateButton.callOnClick()
                }

                updateDateButtonText(viewHolder.dateTextView, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH))

                viewHolder.changeDateButton.setOnClickListener {
                    viewHolder.datePicker.apply { visible = !visible }
                }

                viewHolder.changeEndDateButton.setOnClickListener {
                    viewHolder.endDatePicker.apply { visible = !visible }
                }

                viewHolder.endDatePicker.minDate = now.timeInMillis
                viewHolder.endDatePicker.init(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)) { _, year, month, dayOfMonth ->
                    updateDateButtonText(viewHolder.endDateTextView, year, month, dayOfMonth)
                    viewHolder.changeEndDateButton.callOnClick()
                }

                updateDateButtonText(viewHolder.endDateTextView, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH))

                on<TaskHandler>().activeTask?.let {
                    if (it.taskType == TaskType.CREATE_EVENT_IN_GROUP) {
                        viewHolder.isPublicToggle.check(if (it.group.isPublic) R.id.publicToggleButton else R.id.friendsToggleButton)
                        viewHolder.isPublicToggle.visible = false
                        viewHolder.postEventInContainer.visible = true
                        viewHolder.postEventIn.text = on<ResourcesHandler>().resources.getString(R.string.in_x,
                                it.group.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown))

                        viewHolder.postEventIn.setCompoundDrawablesRelativeWithIntrinsicBounds(if (it.group.isPublic) R.drawable.ic_public_black_24dp else R.drawable.ic_group_black_24dp, 0, 0, 0)

                        viewHolder.removeGroupFromEvent.setOnClickListener {
                            on<TaskHandler>().activeTask = null
                            viewHolder.postEventInContainer.visible = false
                            viewHolder.isPublicToggle.visible = true
                        }
                    }
                }

                viewHolder.scrollView.setOnInterceptTouchListener { _, _ ->
                    if (viewHolder.eventName.hasFocus()) {
                        viewHolder.eventName.clearFocus()
                        viewHolder.scrollView.requestFocus()
                        on<KeyboardHandler>().showKeyboard(viewHolder.eventName, false)
                    }

                    if (viewHolder.eventPrice.hasFocus()) {
                        viewHolder.eventPrice.clearFocus()
                        viewHolder.scrollView.requestFocus()
                        on<KeyboardHandler>().showKeyboard(viewHolder.eventPrice, false)
                    }

                    if (viewHolder.pinnedMessage.hasFocus()) {
                        viewHolder.pinnedMessage.clearFocus()
                        viewHolder.scrollView.requestFocus()
                        on<KeyboardHandler>().showKeyboard(viewHolder.pinnedMessage, false)
                    }

                    false
                }

                alertConfig.alertResult = viewHolder
            }
            buttonClickCallback = { alertResult ->
                    val viewHolder = alertResult as CreateEventViewHolder

                    allDayPreProcess(viewHolder)

                    if (viewHolder.eventName.text.toString().isBlank()) {
                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.enter_event_details))
                        viewHolder.eventName.requestFocus()
                        false
                    } else {
                        var isValid = getViewState(viewHolder).endsAt.time.after(getViewState(viewHolder).startsAt.time)
                        if (!isValid) {
                            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.event_must_not_end_before_it_starts))
                        }

                        if (isValid) {
                            isValid = Date().before(getViewState(viewHolder).endsAt.time)
                            if (!isValid) {
                                on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.event_must_not_end_in_the_past))
                            }
                        }

                        isValid
                    }
                }
                positiveButtonCallback = { alertResult ->
                    val viewHolder = alertResult as CreateEventViewHolder

                    val event = getViewState(viewHolder)
                    val isAllDay = viewHolder.isAllDaySwitch.isChecked

                    allDayPreProcess(viewHolder)

                    createNewEvent(viewHolder.isPublicToggle.checkedButtonId == R.id.publicToggleButton,
                            latLng,
                            viewHolder.eventName.text.toString(),
                            viewHolder.pinnedMessage.text.toString(),
                            viewHolder.eventPrice.text.toString(),
                            event.startsAt.time,
                            event.endsAt.time,
                            isAllDay,
                            onEventCreatedListener)
                }
            show()
        }
    }

    private fun allDayPreProcess(viewHolder: CreateEventViewHolder) {
        if (viewHolder.isAllDaySwitch.isChecked) {
            getViewState(viewHolder).apply {
                startsAt.setToStartOfDay()
                endsAt.setToEndOfDay()
            }
        }
    }

    private fun updateDateButtonText(dateTextView: TextView, year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(year, month, dayOfMonth)
        dateTextView.text = DateUtils.getRelativeTimeSpanString(
                calendar.timeInMillis,
                Calendar.getInstance(TimeZone.getDefault()).timeInMillis,
                DAY_IN_MILLIS
        )
    }

    private fun getViewState(viewHolder: CreateEventViewHolder): CreateEventViewState {
        val startsAt = Calendar.getInstance(TimeZone.getDefault())
        val endsAt = Calendar.getInstance(TimeZone.getDefault())

        startsAt.set(viewHolder.datePicker.year, viewHolder.datePicker.month, viewHolder.datePicker.dayOfMonth,
                viewHolder.startsAtTimePicker.currentHour, viewHolder.startsAtTimePicker.currentMinute, 0)
        endsAt.set(viewHolder.endDatePicker.year, viewHolder.endDatePicker.month, viewHolder.endDatePicker.dayOfMonth,
                viewHolder.endsAtTimePicker.currentHour, viewHolder.endsAtTimePicker.currentMinute, 0)

        return CreateEventViewState(startsAt, endsAt)
    }

    private fun createNewEvent(isPublic: Boolean,
                               latLng: LatLng,
                               name: String,
                               pinnedMessage: String,
                               price: String,
                               startsAt: Date,
                               endsAt: Date,
                               allDay: Boolean,
                               onEventCreatedListener: OnEventCreatedListener) {
        val event = on<StoreHandler>().create(Event::class.java)
        event!!.name = name.trim()
        event.about = price.trim()
        event.isPublic = isPublic
        event.latitude = latLng.latitude
        event.longitude = latLng.longitude
        event.startsAt = startsAt
        event.endsAt = endsAt
        event.allDay = allDay

        val group = on<TaskHandler>().activeTask?.group
        on<TaskHandler>().activeTask = null

        on<SyncHandler>().sync(event) { id ->
            event.id = id

            if (group != null) {
                on<GroupMessageAttachmentHandler>().shareEvent(event, group)
                on<NavigationHandler>().showGroup(group.id!!)
            }

            if (pinnedMessage.isNotBlank()) {
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().getEvent(id).map { EventResult.from(it) }.subscribe({
                    val groupMessage = GroupMessage()
                    groupMessage.text = pinnedMessage
                    groupMessage.from = on<PersistenceHandler>().phoneId
                    groupMessage.to = it.groupId
                    groupMessage.created = Date()

                    val eventGroupId = it.groupId!!

                    on<SyncHandler>().sync(groupMessage) { groupMessageId ->
                        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().addPin(groupMessageId, eventGroupId).subscribe({}, {
                            on<DefaultAlerts>().thatDidntWork()
                        }))
                    }
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }))
            }

            onEventCreatedListener.invoke(event)
        }
    }

    fun eventBubbleFrom(event: Event): MapBubble {
        val mapBubble = MapBubble(LatLng(event.latitude!!, event.longitude!!), "Event", event.name)
        mapBubble.type = BubbleType.EVENT
        mapBubble.isPinned = true
        mapBubble.tag = event
        return mapBubble
    }

    fun hostAgain(event: Event) {
        val daysAgo = TimeUnit.MILLISECONDS.toDays(Date().time - event.startsAt!!.time).toInt()
        val now = Calendar.getInstance(TimeZone.getDefault())
        val startsAt = event.startsAt!!.let { Calendar.getInstance(TimeZone.getDefault()).apply {
            time = it
            add(Calendar.DATE, daysAgo)
        } }
        val endsAt = event.endsAt!!.let { Calendar.getInstance(TimeZone.getDefault()).apply {
            time = it
            add(Calendar.DATE, daysAgo)
        } }

        if (startsAt.before(now)) {
            startsAt.add(Calendar.DATE, 1)
            endsAt.add(Calendar.DATE, 1)
        }

        on<EventHandler>().createNewEvent(
                LatLng(event.latitude!!, event.longitude!!),
                event.isPublic,
                event.name,
                event.about,
                startsAt.time,
                endsAt.time,
                event.allDay
        ) {
            on<GroupActivityTransitionHandler>().showGroupForEvent(null, it)
        }
    }

    fun editReminders(event: Event) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.reminders
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.apply)
            positiveButtonCallback = {
                val items = (it as EditRemindersAdapter).items
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().updateEventReminders(event.id!!, items).subscribe({
                    event.reminders = items
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }))
            }

            onAfterViewCreated = { alertConfig, view ->

                lateinit var adapter: EditRemindersAdapter

                adapter = EditRemindersAdapter(on) {
                    adapter.items = mutableListOf<EventReminder>().apply {
                        addAll(adapter.items)
                        remove(it)
                    }
                }

                alertConfig.alertResult = adapter

                adapter.items = mutableListOf()

                view.remindersRecyclerView.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
                view.remindersRecyclerView.adapter = adapter

                view.addReminder.setOnClickListener {
                    adapter.items = mutableListOf<EventReminder>().apply {
                        addAll(adapter.items)
                        add(EventReminder().apply { offset.amount = -1; offset.unit = EventReminderOffsetUnit.Hour })
                    }
                    view.remindersRecyclerView.apply {
                        post {
                            smoothScrollToPosition(adapter.items.size - 1)
                        }
                    }
                }
            }
            show()
        }
    }

    private class CreateEventViewHolder(view: View) {
        var isPublicToggle: MaterialButtonToggleGroup = view.isPublicToggle
        var isAllDaySwitch: Switch = view.isAllDaySwitch
        var changeEndDateButton: View = view.changeEndDate
        var endDateTextView: TextView = view.endDateTextView
        var endDatePicker: DatePicker = view.endsDatePicker
        var startsAtTimePicker: TimePicker = view.startsAt
        var endsAtTimePicker: TimePicker = view.endsAt
        var datePicker: DatePicker = view.datePicker
        var dateTextView: TextView = view.dateTextView
        var changeDateButton: View = view.changeDate
        var eventName: EditText = view.name
        var pinnedMessage: EditText = view.pinnedMessage
        var eventPrice: EditText = view.price
        var postEventInContainer: ViewGroup = view.postEventInContainer
        var postEventIn: TextView = view.postEventIn
        var removeGroupFromEvent: ImageButton = view.removeGroupFromEvent
        var scrollView: InterceptableScrollView = view as InterceptableScrollView
    }

    private class CreateEventViewState(var startsAt: Calendar, var endsAt: Calendar)
}

typealias OnEventCreatedListener = (event: Event) -> Unit
