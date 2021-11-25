package closer.vlllage.com.closer.handler.feed.content

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.event.EventAdapter
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Event_
import at.bluesource.choicesdk.maps.common.LatLng
import closer.vlllage.com.closer.databinding.CalendarDayItemBinding
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.calendar_event_item.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class CalendarDayMixedItem(val position: Int, val date: Date) : MixedItem(MixedItemType.CalendarDay)

class CalendarDayViewHolder(val binding: CalendarDayItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.CalendarDay) {
    var disposable: Disposable? = null
    lateinit var on: On
    var eventsObservable: DataSubscription? = null
    val views = mutableSetOf<View>()
    lateinit var allDayAdapter: EventAdapter
    var events: List<Event>? = null
}

class CalendarDayMixedItemAdapter(private val on: On) : MixedItemAdapter<CalendarDayMixedItem, CalendarDayViewHolder> {
    override fun bind(holder: CalendarDayViewHolder, item: CalendarDayMixedItem, position: Int) {
        bindCalendarDay(holder, item.date, position)
    }

    override fun getMixedItemClass() = CalendarDayMixedItem::class
    override fun getMixedItemType() = MixedItemType.CalendarDay

    override fun areItemsTheSame(old: CalendarDayMixedItem, new: CalendarDayMixedItem) = old.date == new.date

    override fun areContentsTheSame(old: CalendarDayMixedItem, new: CalendarDayMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = CalendarDayViewHolder(CalendarDayItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: CalendarDayViewHolder) {
        holder.on.off()
    }

    private fun bindCalendarDay(holder: CalendarDayViewHolder, date: Date, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.binding.date.text = on<TimeStr>().day(date)

        holder.allDayAdapter = EventAdapter(on)
        holder.binding.allDayEvents.adapter = holder.allDayAdapter
        holder.binding.allDayEvents.layoutManager = LinearLayoutManager(holder.binding.allDayEvents.context, LinearLayoutManager.HORIZONTAL, false)

        holder.binding.headerPadding.visible = false && position == 0
        holder.binding.headerPadding.clipToOutline = true

        holder.binding.day.clipToOutline = true

        holder.binding.day.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val vH = holder.binding.day.measuredHeight
                val h = floor((event.y / vH) * TimeUnit.DAYS.toHours(1)).toInt()

                var startsAt = Calendar.getInstance(TimeZone.getDefault()).let {
                    it.time = date
                    it.set(Calendar.HOUR_OF_DAY, h)
                    it.time
                }

                on<LocationHandler>().getCurrentLocation {
                    on<EventHandler>().createNewEvent(
                        LatLng(
                            it.latitude,
                            it.longitude
                        ), false, startsAt = startsAt) {
                        on<GroupActivityTransitionHandler>().showGroupForEvent(null, it)
                    }
                }

                true
            } else true
        }

        val distance = on<HowFar>().about7Miles
        val dateStart = Date(date.time + 1)
        val dateEnd = Date(date.time + TimeUnit.DAYS.toMillis(1) - 1)
        val isToday = DateUtils.isToday(dateStart.time)

        holder.binding.pastTime.visible = isToday

        // TODO also track future days
        if (isToday) {
            trackTimeOfDay(holder)
        }

        holder.on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
                    holder.eventsObservable?.let { holder.on<DisposableHandler>().dispose(it) }
                    holder.eventsObservable = on<StoreHandler>().store.box(Event::class).query(
                            Event_.startsAt.less(dateEnd).and(Event_.endsAt.greater(dateStart)
                            ).and(
                                    Event_.latitude.between(cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance).and(
                                            Event_.longitude.between(cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                                    ).or(
                                            Event_.isPublic.equal(false)
                                    )
                            )
                    )
                            .build()
                            .subscribe()
                            .on(AndroidScheduler.mainThread())
                            .observer {
                                setAllDayEvents(holder, it.filter { it.allDay })
                                setCalendarDayEvents(holder, date, it.filter { it.allDay.not() })
                            }.also {
                                holder.on<DisposableHandler>().add(it)
                            }
                })
    }

    private fun trackTimeOfDay(holder: CalendarDayViewHolder) {
        holder.on<TimerHandler>().postDisposable({
            val now = Calendar.getInstance(TimeZone.getDefault())
            val percentDayPast = (
                    now.get(Calendar.HOUR_OF_DAY) * TimeUnit.HOURS.toSeconds(1) +
                    now.get(Calendar.MINUTE) * TimeUnit.MINUTES.toSeconds(1)
                    ).toFloat() / TimeUnit.DAYS.toSeconds(1)

            holder.binding.pastTime.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintPercentHeight = percentDayPast
            }

            trackTimeOfDay(holder)
        }, 1000)
    }

    private fun setAllDayEvents(holder: CalendarDayViewHolder, events: List<Event>) {
        holder.binding.allDayEvents.visible = events.isEmpty().not()
        holder.allDayAdapter.items = events.sortedBy { it.startsAt }.toMutableList()
    }

    private fun setCalendarDayEvents(holder: CalendarDayViewHolder, date: Date, events: List<Event>? = null) {
        holder.views.forEach { holder.binding.day.removeView(it) }
        holder.views.clear()

        if (events != null) {
            holder.events = events
        }

        val vH = holder.binding.day.measuredHeight
        val minH = ((TimeUnit.HOURS.toMillis(1) ).toFloat() / TimeUnit.DAYS.toMillis(1) * vH).toInt()

        if (vH == 0) {
            holder.itemView.post { setCalendarDayEvents(holder, date, events) }
            return
        }

        val dayOfYear = Calendar.getInstance(TimeZone.getDefault()).let {
            it.time = date
            it.get(Calendar.DAY_OF_YEAR)
        }

        val overlapping = Overlapping()

        holder.disposable?.dispose()
        holder.disposable = holder.on<ApiHandler>().getEventRemindersOnDay(date).subscribe({
            it.forEach { eventReminder ->
                eventReminder.instances?.forEach { instance ->
                    val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.calendar_reminder_item, holder.binding.day, false)

                    view.name.text = eventReminder.text?.let { "$it${eventReminder.event?.name?.let { " ($it)" } ?: ""}" } ?: eventReminder.event?.name
                    (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                        width = ViewGroup.LayoutParams.MATCH_PARENT
                        height = minH
                        constrainedHeight = true
                        constrainedWidth = true
                        marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3 +
                                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 4 * overlapping.count(
                            Overlapping.Companion.Entry(instance, Date(instance.time + TimeUnit.HOURS.toMillis(1))))
                        marginEnd = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
                    }

                    overlapping.add(Overlapping.Companion.Entry(instance, Date(instance.time + TimeUnit.HOURS.toMillis(1))))

                    view.translationY = (vH * Calendar.getInstance(TimeZone.getDefault()).let {
                        it.time = instance
                        (it.get(Calendar.DAY_OF_YEAR) - dayOfYear).toFloat() +
                                it.get(Calendar.HOUR_OF_DAY).toFloat() / TimeUnit.DAYS.toHours(1).toFloat() +
                                it.get(Calendar.MINUTE).toFloat() / TimeUnit.DAYS.toMinutes(1).toFloat()
                    })

                    view.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_baseline_flag_18dp, 0, 0, 0
                    )

                    view.setBackgroundResource(R.drawable.clickable_blue_8dp)

                    view.setOnClickListener {
                        eventReminder.event?.let { on<GroupActivityTransitionHandler>().showGroupForEvent(view, EventResult.from(it)) }
                    }

                    holder.binding.day.addView(view)
                    holder.views.add(view)
                }
            }
        }, {
            on<DefaultAlerts>().syncError()
        }).also {
            holder.on<DisposableHandler>().add(it)
        }

        holder.events?.sortedBy { it.startsAt }?.onEach { event ->
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.calendar_event_item, holder.binding.day, false)

            view.name.text = event.name
            view.about.text = on<EventDetailsHandler>().formatEventDetails(event)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                val h = (event.endsAt!!.time - event.startsAt!!.time).toFloat() / TimeUnit.DAYS.toMillis(1) * vH

                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = h.toInt().coerceAtLeast(minH)
                constrainedHeight = true
                constrainedWidth = true
                marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3 +
                        on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 4 * overlapping.count(Overlapping.Companion.Entry(event.startsAt!!, event.endsAt!!))
                marginEnd = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
            }

            overlapping.add(Overlapping.Companion.Entry(event.startsAt!!, event.endsAt!!))

            view.translationY = (vH * Calendar.getInstance(TimeZone.getDefault()).let {
                it.time = event.startsAt!!
                (it.get(Calendar.DAY_OF_YEAR) - dayOfYear).toFloat() +
                        it.get(Calendar.HOUR_OF_DAY).toFloat() / TimeUnit.DAYS.toHours(1).toFloat() +
                        it.get(Calendar.MINUTE).toFloat() / TimeUnit.DAYS.toMinutes(1).toFloat()
            })

            view.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                if (event.isPublic) R.drawable.ic_public_black_18dp else R.drawable.ic_group_black_18dp, 0, 0, 0
            )

            view.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForEvent(view, event)
            }

            holder.binding.day.addView(view)
            holder.views.add(view)
        }
    }
}

class Overlapping {

    companion object {
        data class Entry constructor(
            val startsAt: Date,
            val endsAt: Date
        )
    }

    val entries = mutableListOf<Entry>()

    fun add(entry: Entry) {
        entries.add(entry)
    }

    fun count(entry: Entry) = entries.count {
        it.startsAt.before(entry.endsAt) && it.endsAt.after(entry.startsAt)
    }
}

