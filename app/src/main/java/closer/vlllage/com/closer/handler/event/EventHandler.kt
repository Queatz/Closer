package closer.vlllage.com.closer.handler.event

import android.os.Build
import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.view.View
import android.widget.*
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.BubbleType
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.ui.InterceptableScrollView
import com.google.android.gms.maps.model.LatLng
import java.util.*

class EventHandler constructor(private val on: On) {

    fun createNewEvent(latLng: LatLng, isPublic: Boolean, onEventCreatedListener: OnEventCreatedListener) {
        on<AlertHandler>().make().apply {
            theme = R.style.AppTheme_AlertDialog_Red
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.post_event)
            layoutResId = R.layout.post_event_modal
            onAfterViewCreated = { alertConfig, view ->
                val viewHolder = CreateEventViewHolder(view)

                val now = Calendar.getInstance(TimeZone.getDefault())
                viewHolder.startsAtTimePicker.currentHour = (now.get(Calendar.HOUR_OF_DAY) + 1) % 24
                viewHolder.startsAtTimePicker.currentMinute = 0
                viewHolder.endsAtTimePicker.currentHour = (now.get(Calendar.HOUR_OF_DAY) + 4) % 24
                viewHolder.endsAtTimePicker.currentMinute = 0
                viewHolder.datePicker.visibility = View.GONE

                viewHolder.datePicker.minDate = now.timeInMillis
                viewHolder.isPublicSwitch.isChecked = isPublic

                viewHolder.datePicker.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)) { datePicker1, year, month, dayOfMonth ->
                    val calendar = Calendar.getInstance(TimeZone.getDefault())
                    calendar.set(year, month, dayOfMonth)
                    viewHolder.dateTextView.text = DateUtils.getRelativeTimeSpanString(
                            calendar.timeInMillis,
                            now.timeInMillis,
                            DAY_IN_MILLIS
                    )
                    viewHolder.changeDateButton.callOnClick()
                }

                viewHolder.changeDateButton.setOnClickListener { viewHolder.datePicker.visibility = if (viewHolder.datePicker.visibility == View.GONE) View.VISIBLE else View.GONE }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    viewHolder.scrollView.setOnInterceptTouchListener(View.OnTouchListener { v, event ->
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

                        false
                    })
                }

                alertConfig.alertResult = viewHolder
            }
            buttonClickCallback = { alertResult ->
                    val viewHolder = alertResult as CreateEventViewHolder

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

                    createNewEvent(viewHolder.isPublicSwitch.isChecked,
                            latLng,
                            viewHolder.eventName.text.toString(),
                            viewHolder.eventPrice.text.toString(),
                            event.startsAt.time,
                            event.endsAt.time,
                            onEventCreatedListener)
                }
            title = on<ResourcesHandler>().resources.getString(R.string.post_event)
            show()
        }
    }

    private fun getViewState(viewHolder: CreateEventViewHolder): CreateEventViewState {
        val startsAt = Calendar.getInstance(TimeZone.getDefault())
        val endsAt = Calendar.getInstance(TimeZone.getDefault())

        startsAt.set(viewHolder.datePicker.year, viewHolder.datePicker.month, viewHolder.datePicker.dayOfMonth,
                viewHolder.startsAtTimePicker.currentHour, viewHolder.startsAtTimePicker.currentMinute, 0)
        endsAt.set(viewHolder.datePicker.year, viewHolder.datePicker.month, viewHolder.datePicker.dayOfMonth,
                viewHolder.endsAtTimePicker.currentHour, viewHolder.endsAtTimePicker.currentMinute, 0)

        if (viewHolder.isNextDaySwitch.isChecked) {
            endsAt.add(Calendar.DATE, 1)
        }

        return CreateEventViewState(startsAt, endsAt)
    }

    private fun createNewEvent(isPublic: Boolean, latLng: LatLng, name: String, price: String, startsAt: Date, endsAt: Date, onEventCreatedListener: OnEventCreatedListener) {
        val event = on<StoreHandler>().create(Event::class.java)
        event!!.name = name.trim()
        event.about = price.trim()
        event.isPublic = isPublic
        event.latitude = latLng.latitude
        event.longitude = latLng.longitude
        event.startsAt = startsAt
        event.endsAt = endsAt
        on<StoreHandler>().store.box(Event::class).put(event)
        on<SyncHandler>().sync(event)
        onEventCreatedListener.invoke(event)
    }

    fun eventBubbleFrom(event: Event): MapBubble {
        val mapBubble = MapBubble(LatLng(event.latitude!!, event.longitude!!), "Event", event.name)
        mapBubble.type = BubbleType.EVENT
        mapBubble.isPinned = true
        mapBubble.tag = event
        return mapBubble
    }

    private class CreateEventViewHolder internal constructor(view: View) {
        internal var isPublicSwitch: Switch = view.findViewById(R.id.isPublicSwitch)
        internal var isNextDaySwitch: Switch = view.findViewById(R.id.isNextDaySwitch)
        internal var startsAtTimePicker: TimePicker = view.findViewById(R.id.startsAt)
        internal var endsAtTimePicker: TimePicker = view.findViewById(R.id.endsAt)
        internal var datePicker: DatePicker = view.findViewById(R.id.datePicker)
        internal var dateTextView: TextView = view.findViewById(R.id.dateTextView)
        internal var changeDateButton: View = view.findViewById(R.id.changeDate)
        internal var eventName: EditText = view.findViewById(R.id.name)
        internal var eventPrice: EditText = view.findViewById(R.id.price)
        internal var scrollView: InterceptableScrollView = view as InterceptableScrollView
    }

    private inner class CreateEventViewState(internal var startsAt: Calendar, internal var endsAt: Calendar)
}

typealias OnEventCreatedListener = (event: Event) -> Unit
