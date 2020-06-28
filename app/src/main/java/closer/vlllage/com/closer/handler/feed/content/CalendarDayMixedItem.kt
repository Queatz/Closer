package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.HowFar
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.TimeStr
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

        holder.itemView.headerPadding.visible = false && position == 0
        holder.itemView.headerPadding.clipToOutline = true

        holder.day.clipToOutline = true

        val distance = on<HowFar>().about7Miles
        val dateStart = Date(date.time + 1)
        val dateEnd = Date(date.time + TimeUnit.DAYS.toMillis(1) - 1)

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
                            .observer { setCalendarDayEvents(holder, date, it) }.also {
                                holder.on<DisposableHandler>().add(it)
                            }
                })
    }

    private fun setCalendarDayEvents(holder: CalendarDayViewHolder, date: Date, events: List<Event>? = null) {
        holder.views.forEach { holder.day.removeView(it) }
        holder.views.clear()

        if (events != null) {
            holder.events = events
        }

        val vH = holder.day.measuredHeight

        if (vH == 0) {
            holder.itemView.post { setCalendarDayEvents(holder, date, events) }
            return
        }

        val dayOfYear = Calendar.getInstance(TimeZone.getDefault()).let {
            it.time = date
            it.get(Calendar.DAY_OF_YEAR)
        }

        holder.events?.forEach { event ->
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.calendar_event_item, holder.day, false)

            view.name.text = event.name
            view.about.text = on<EventDetailsHandler>().formatEventDetails(event)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                val h = (event.endsAt!!.time - event.startsAt!!.time).toFloat() / TimeUnit.DAYS.toMillis(1) * vH

                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = h.toInt()
                constrainedHeight = true
                constrainedWidth = true
                marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
                marginEnd = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
            }

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
