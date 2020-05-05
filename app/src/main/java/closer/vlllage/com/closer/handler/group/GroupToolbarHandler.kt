package closer.vlllage.com.closer.handler.group

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

class GroupToolbarHandler constructor(private val on: On) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ToolbarAdapter
    val contentView = BehaviorSubject.createDefault(ContentViewType.MESSAGES)

    fun attach(recyclerView: RecyclerView, onToolbarItemSelected: (ToolbarItem) -> Unit) {
        this.recyclerView = recyclerView
        adapter = ToolbarAdapter(on, onToolbarItemSelected)

        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter

        on<DisposableHandler>().add(contentView.subscribe {
            show(on<GroupHandler>().group)
            adapter.selectedContentView.onNext(it)
        })

        adapter.selectedContentView.distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
            scrollToolbarTo(it)
        }.also {
            on<DisposableHandler>().add(it)
        }

        on<GroupHandler>().onGroupUpdated { show(it) }
        on<GroupHandler>().onGroupChanged { show(it) }
        on<GroupHandler>().onEventChanged { show(on<GroupHandler>().group) }
        on<GroupHandler>().onPhoneChanged { show(on<GroupHandler>().group) }
    }

    private fun scrollToolbarTo(content: ContentViewType) {
        adapter.items.indexOfFirst { it.value == content }.takeIf { it >= 0 }?.let { index ->
            recyclerView.smoothScrollToPosition(index)
        }
    }

    private fun show(group: Group?) {
        if (group == null) {
            return
        }

        val event = if (group.hasEvent()) on<GroupHandler>().event else null

        val items = mutableListOf<ToolbarItem>()

        if (group.hasPhone()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.about),
                    R.drawable.ic_person_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.PHONE_ABOUT)
                    },
                    ContentViewType.PHONE_ABOUT
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.posts),
                    R.drawable.ic_view_day_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.PHONE_MESSAGES)
                    },
                    ContentViewType.PHONE_MESSAGES
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.photos),
                    R.drawable.ic_photo_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.PHONE_PHOTOS)
                    },
                    ContentViewType.PHONE_PHOTOS
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.groups),
                    R.drawable.ic_group_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.PHONE_GROUPS)
                    },
                    ContentViewType.PHONE_GROUPS
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.invite),
                    R.drawable.ic_person_add_black_24dp,
                    View.OnClickListener {
                        on<ShareActivityTransitionHandler>().inviteToGroup(group.phoneId!!)
                    }
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.show_on_map),
                    R.drawable.ic_my_location_black_24dp,
                    View.OnClickListener {
                        val runnable = { on<MapActivityHandler>().showPhoneOnMap(group.phoneId) }
                        if (on<ActivityHandler>().activity is CircularRevealActivity) {
                            (on<ActivityHandler>().activity as CircularRevealActivity).finish(runnable)
                        } else {
                            runnable.invoke()
                        }
                    }
            ))

            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.get_directions),
                    R.drawable.ic_directions_black_24dp,
                    View.OnClickListener {
                        on<DataHandler>().getPhone(group.phoneId!!).subscribe({
                            if (it.latitude != null && it.longitude != null) {
                                on<OutboundHandler>().openDirections(LatLng(it.latitude!!, it.longitude!!
                                ))
                            } else {
                                on<DefaultAlerts>().thatDidntWork()
                            }
                        }, {
                            on<DefaultAlerts>().thatDidntWork()
                        })
                    }
            ))
        }

        if (group.physical || (!group.hasEvent() && !group.isPublic)) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.events),
                    R.drawable.ic_event_available_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.EVENTS)
                    },
                    ContentViewType.EVENTS
            ))
        }

        if (group.ratingCount ?: 0 > 0 && (group.physical || group.hasEvent())) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getQuantityString(R.plurals.review_count, group.ratingCount!!, group.ratingCount!!),
                    R.drawable.ic_star_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.REVIEWS)
                    },
                    ContentViewType.REVIEWS
            ))
        }

        if (!group.hasPhone()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.members),
                    R.drawable.ic_group_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.CONTACTS)
                    },
                    ContentViewType.CONTACTS
            ))
        }

        if (!group.hasPhone() && group.isPublic && on<GroupMemberHandler>().isCurrentUserMemberOf(group).not()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(if (group.hasEvent()) R.string.join_event else R.string.join_group),
                    R.drawable.ic_person_add_black_24dp,
                    View.OnClickListener {
                        on<GroupMemberHandler>().join(group)
                    }
            ))
        }

        if (isEventEnded(event)) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.host_again),
                    R.drawable.ic_refresh_black_24dp,
                    View.OnClickListener {
                        event?.let {
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
                                    LatLng(it.latitude!!, it.longitude!!),
                                    it.isPublic,
                                    event.name,
                                    event.about,
                                    startsAt.time,
                                    endsAt.time
                            ) {
                                on<GroupActivityTransitionHandler>().showGroupForEvent(null, it)
                            }
                        }
                    }
            ))
        }

        if (event != null) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.share),
                    R.drawable.ic_share_black_24dp,
                    View.OnClickListener { shareEvent(event) }
            ))
        }

        if (event != null || group.physical) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.show_on_map),
                    R.drawable.ic_my_location_black_24dp,
                    View.OnClickListener { v ->
                        if (event != null)
                            showEventOnMap(event)
                        else
                            showGroupOnMap(group)
                    }
            ))
        }

        if (group.physical && group.name.isNullOrBlank()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.set_name),
                    R.drawable.ic_edit_location_black_24dp,
                    View.OnClickListener { v ->
                        on<PhysicalGroupUpgradeHandler>().convertToHub(group) { updatedGroup ->
                            on<RefreshHandler>().refresh(updatedGroup)
                            show(updatedGroup)
                        }
                    }
            ))
        }

        if (group.physical && group.photo.isNullOrBlank()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.set_background),
                    R.drawable.ic_camera_black_24dp,
                    View.OnClickListener { v ->
                        on<PhysicalGroupUpgradeHandler>().setBackground(group) { updatedGroup ->
                            on<RefreshHandler>().refresh(updatedGroup)
                            show(updatedGroup)
                        }
                    }
            ))
        }

        if (!group.hasPhone() && !group.hasEvent()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.add_action),
                    R.drawable.ic_add_black_24dp,
                    View.OnClickListener {
                        on<GroupActionHandler>().addActionToGroup(group)
                    }
            ))
        }

        if ((event != null || group.physical)) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.get_directions),
                    R.drawable.ic_directions_black_24dp,
                    View.OnClickListener { v ->
                        on<OutboundHandler>().openDirections(LatLng(
                                group.latitude!!,
                                group.longitude!!
                        ))
                    }
            ))
        }

        if (group.physical || (!group.hasEvent() && !group.isPublic)) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.host_event),
                    R.drawable.ic_event_note_black_24dp,
                    View.OnClickListener {
                        on<HostEventHelper>().hostEvent(group)
                    }
            ))
        }

        if (group.physical || group.hasEvent()) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.write_review),
                    R.drawable.ic_star_half_black_24dp,
                    View.OnClickListener {
                        on<ReviewHandler>().postReview(group)
                    }
            ))
        }

        if (!group.hasPhone() && items.isNotEmpty()) {
            items.add(0, ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.posts),
                    R.drawable.ic_view_day_black_24dp,
                    View.OnClickListener {
                        contentView.onNext(ContentViewType.MESSAGES)
                    },
                    ContentViewType.MESSAGES
            ))
        }

        if (isEventCancelable(event)) {
            items.add(ToolbarItem(
                    on<ResourcesHandler>().resources.getString(R.string.cancel),
                    R.drawable.ic_close_black_24dp,
                    View.OnClickListener {
                        on<AlertHandler>().make().apply {
                            title = on<ResourcesHandler>().resources.getString(R.string.cancel_event)
                            message = on<ResourcesHandler>().resources.getString(R.string.event_will_be_cancelled, event!!.name)
                            positiveButton = on<ResourcesHandler>().resources.getString(R.string.cancel_event)
                            positiveButtonCallback = { result ->
                                on<DisposableHandler>().add(on<ApiHandler>().cancelEvent(event.id!!).subscribe({ successResult ->
                                    if (successResult.success) {
                                        on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.event_cancelled, event.name))
                                        on<RefreshHandler>().refreshEvents(LatLng(event.latitude!!, event.longitude!!))
                                    } else {
                                        on<DefaultAlerts>().thatDidntWork()
                                    }
                                }, { on<DefaultAlerts>().thatDidntWork() }))
                            }
                            show()
                        }
                    }
            ))
        }

        adapter.items = items
    }

    private fun shareEvent(event: Event) {
        on<ShareActivityTransitionHandler>().shareEvent(event.id!!)
    }

    private fun isEventCancelable(event: Event?): Boolean {
        return event != null && on<PersistenceHandler>().phoneId != null &&
                !event.cancelled && event.creator != null &&
                Date().before(event.endsAt) &&
                event.creator == on<PersistenceHandler>().phoneId
    }

    private fun isEventEnded(event: Event?) = event != null && Date().after(event.endsAt)

    private fun showGroupOnMap(group: Group?) {
        (on<ActivityHandler>().activity as CircularRevealActivity)
                .finish { on<MapActivityHandler>().showGroupOnMap(group!!) }
    }

    private fun showEventOnMap(event: Event?) {
        (on<ActivityHandler>().activity as CircularRevealActivity)
                .finish { on<MapActivityHandler>().showEventOnMap(event!!) }
    }

    class ToolbarItem(
            var name: String,
            @field:DrawableRes var icon: Int,
            var onClickListener: View.OnClickListener,
            var value: ContentViewType? = null,
            @field:ColorRes var color: Int? = null,
            val indicator: BehaviorSubject<Boolean>? = null
    )
}
