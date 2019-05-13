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
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.ui.InterceptableScrollView
import com.google.android.gms.maps.model.LatLng
import java.util.*

class EventHandler : PoolMember() {

    fun createNewEvent(latLng: LatLng, isPublic: Boolean, onEventCreatedListener: OnEventCreatedListener) {
        `$`(AlertHandler::class.java).make().apply {
            theme = R.style.AppTheme_AlertDialog_Red
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.post_event)
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
                            `$`(KeyboardHandler::class.java).showKeyboard(viewHolder.eventName, false)
                        }

                        if (viewHolder.eventPrice.hasFocus()) {
                            viewHolder.eventPrice.clearFocus()
                            viewHolder.scrollView.requestFocus()
                            `$`(KeyboardHandler::class.java).showKeyboard(viewHolder.eventPrice, false)
                        }

                        false
                    })
                }

                alertConfig.alertResult = viewHolder
            }
            buttonClickCallback = { alertResult ->
                    val viewHolder = alertResult as CreateEventViewHolder

                    if (viewHolder.eventName.text.toString().trim { it <= ' ' }.isEmpty()) {
                        `$`(DefaultAlerts::class.java).message(`$`(ResourcesHandler::class.java).resources.getString(R.string.enter_event_details))
                        viewHolder.eventName.requestFocus()
                        false
                    } else {
                        var isValid = getViewState(viewHolder).endsAt.time.after(getViewState(viewHolder).startsAt.time)
                        if (!isValid) {
                            `$`(DefaultAlerts::class.java).message(`$`(ResourcesHandler::class.java).resources.getString(R.string.event_must_not_end_before_it_starts))
                        }

                        if (isValid) {
                            isValid = Date().before(getViewState(viewHolder).endsAt.time)
                            if (!isValid) {
                                `$`(DefaultAlerts::class.java).message(`$`(ResourcesHandler::class.java).resources.getString(R.string.event_must_not_end_in_the_past))
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
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.post_event)
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
        val event = `$`(StoreHandler::class.java).create(Event::class.java)
        event!!.name = name.trim { it <= ' ' }
        event.about = price.trim { it <= ' ' }
        event.isPublic = isPublic
        event.latitude = latLng.latitude
        event.longitude = latLng.longitude
        event.startsAt = startsAt
        event.endsAt = endsAt
        `$`(StoreHandler::class.java).store.box(Event::class.java).put(event)
        `$`(SyncHandler::class.java).sync(event)
        onEventCreatedListener.onEventCreated(event)
    }

    fun eventBubbleFrom(event: Event): MapBubble {
        val mapBubble = MapBubble(LatLng(event.latitude!!, event.longitude!!), "Event", event.name)
        mapBubble.type = BubbleType.EVENT
        mapBubble.isPinned = true
        mapBubble.tag = event
        return mapBubble
    }

    private class CreateEventViewHolder internal constructor(view: View) {
        internal var isPublicSwitch: Switch
        internal var isNextDaySwitch: Switch
        internal var startsAtTimePicker: TimePicker
        internal var endsAtTimePicker: TimePicker
        internal var datePicker: DatePicker
        internal var dateTextView: TextView
        internal var changeDateButton: View
        internal var eventName: EditText
        internal var eventPrice: EditText
        internal var scrollView: InterceptableScrollView

        init {
            this.isPublicSwitch = view.findViewById(R.id.isPublicSwitch)
            this.isNextDaySwitch = view.findViewById(R.id.isNextDaySwitch)
            this.startsAtTimePicker = view.findViewById(R.id.startsAt)
            this.endsAtTimePicker = view.findViewById(R.id.endsAt)
            this.datePicker = view.findViewById(R.id.datePicker)
            this.dateTextView = view.findViewById(R.id.dateTextView)
            this.changeDateButton = view.findViewById(R.id.changeDate)
            this.eventName = view.findViewById(R.id.name)
            this.eventPrice = view.findViewById(R.id.price)
            this.scrollView = view as InterceptableScrollView
        }
    }

    private inner class CreateEventViewState(internal var startsAt: Calendar, internal var endsAt: Calendar)

    interface OnEventCreatedListener {
        fun onEventCreated(event: Event)
    }
}
