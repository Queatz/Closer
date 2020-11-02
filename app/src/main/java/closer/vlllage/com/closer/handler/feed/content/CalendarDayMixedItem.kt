package closer.vlllage.com.closer.handler.feed.content

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.event.EventAdapter
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Event_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.calendar_day_item.view.*
import kotlinx.android.synthetic.main.calendar_event_item.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class CalendarDayMixedItem(val position: Int, val date: Date) : MixedItem(MixedItemType.CalendarDay)

class CalendarDayViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.CalendarDay) {
    lateinit var on: On
    var eventsObservable: DataSubscription? = null
    val views = mutableSetOf<View>()
    var date = itemView.date!!
    lateinit var allDayAdapter: EventAdapter
    var allDayEvents = itemView.allDayEvents!!
    var day = itemView.day!!
    var events: List<Event>? = null
}

class CalendarDayMixedItemAdapter(private val on: On) : MixedItemAdapter<CalendarDayMixedItem, CalendarDayViewHolder> {
    override fun bind(holder: CalendarDayViewHolder, item: CalendarDayMixedItem, position: Int) {
        bindCalendarDay(holder, item.date, position)
    }

    override fun getMixedItemClass() = CalendarDayMixedItem::class
    override fun getMixedItemType() = MixedItemType.CalendarDay

    override fun areItemsTheSame(old: CalendarDayMixedItem, new: CalendarDayMixedItem) = false

    override fun areContentsTheSame(old: CalendarDayMixedItem, new: CalendarDayMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = CalendarDayViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.calendar_day_item, parent, false))

    override fun onViewRecycled(holder: CalendarDayViewHolder) {
        holder.on.off()
    }

    private fun bindCalendarDay(holder: CalendarDayViewHolder, date: Date, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.date.text = on<TimeStr>().day(date)

        holder.allDayAdapter = EventAdapter(on)
        holder.allDayEvents.adapter = holder.allDayAdapter
        holder.allDayEvents.layoutManager = LinearLayoutManager(holder.allDayEvents.context, LinearLayoutManager.HORIZONTAL, false)

        holder.itemView.headerPadding.visible = false && position == 0
        holder.itemView.headerPadding.clipToOutline = true

        holder.day.clipToOutline = true

        val distance = on<HowFar>().about7Miles
        val dateStart = Date(date.time + 1)
        val dateEnd = Date(date.time + TimeUnit.DAYS.toMillis(1) - 1)
        val isToday = DateUtils.isToday(dateStart.time)

        holder.itemView.pastTime.visible = isToday

        if (isToday) {
            trackTimeOfDay(holder)
        }

        holder.on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
                    holder.eventsObservable?.let { holder.on<DisposableHandler>().dispose(it) }
                    holder.eventsObservable = on<StoreHandler>().store.box(Event::class).query(
                            Event_.startsAt.between(dateStart, dateEnd).or(Event_.endsAt.between(dateStart, dateEnd)
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
            val percentDayPast = (now.get(Calendar.HOUR_OF_DAY) * TimeUnit.HOURS.toSeconds(1)).toFloat() / TimeUnit.DAYS.toSeconds(1)

            holder.itemView.pastTime.updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintPercentHeight = percentDayPast
            }

            trackTimeOfDay(holder)
        }, 1000)
    }

    private fun setAllDayEvents(holder: CalendarDayViewHolder, events: List<Event>) {
        holder.allDayEvents.visible = events.isEmpty().not()
        holder.allDayAdapter.items = events.sortedBy { it.startsAt }.toMutableList()
    }

    private fun setCalendarDayEvents(holder: CalendarDayViewHolder, date: Date, events: List<Event>? = null) {
        holder.views.forEach { holder.day.removeView(it) }
        holder.views.clear()

        if (events != null) {
            holder.events = events
        }

        val vH = holder.day.measuredHeight
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

        holder.events?.sortedBy { it.startsAt }?.forEach { event ->
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.calendar_event_item, holder.day, false)

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
                        on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 4 * overlapping.count(event)
                marginEnd = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
            }

            overlapping.add(event)

            view.translationY = (vH * Calendar.getInstance(TimeZone.getDefault()).let {
                it.time = event.startsAt!!
                (it.get(Calendar.DAY_OF_YEAR) - dayOfYear).toFloat() +
                        it.get(Calendar.HOUR_OF_DAY).toFloat() / TimeUnit.DAYS.toHours(1).toFloat() +
                        it.get(Calendar.MINUTE).toFloat() / TimeUnit.DAYS.toMinutes(1).toFloat()
            })

            view.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (event.isPublic) R.drawable.ic_public_black_18dp else R.drawable.ic_group_black_18dp, 0, 0, 0
            )

//            view.setBackgroundResource(if (event.isPublic) R.drawable.clickable_red_8dp else R.drawable.clickable_blue_8dp)

            view.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForEvent(view, event)
            }

            holder.day.addView(view)
            holder.views.add(view)
        }
    }
}

class Overlapping {
    val events = mutableListOf<Event>()

    fun add(event: Event) {
        events.add(event)
    }

    fun count(event: Event) = events.count {
        it.startsAt!!.before(event.endsAt) && it.endsAt!!.after(event.startsAt)
    }
}