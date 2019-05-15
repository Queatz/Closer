package closer.vlllage.com.closer.handler.group

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.google.android.gms.common.util.Strings.isEmptyOrWhitespace
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class GroupToolbarHandler constructor(private val on: On) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ToolbarAdapter
    val isShareActiveObservable = BehaviorSubject.createDefault(false)

    private var group: Group? = null

    fun attach(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        adapter = ToolbarAdapter(on)

        recyclerView.layoutManager = object : LinearLayoutManager(recyclerView.context, HORIZONTAL, false) {
            override fun measureChild(child: View, widthUsed: Int, heightUsed: Int) {
                child.layoutParams.width = recyclerView.measuredWidth / adapter.itemCount
                super.measureChild(child, widthUsed, heightUsed)
            }

            override fun measureChildWithMargins(child: View, widthUsed: Int, heightUsed: Int) {
                child.layoutParams.width = recyclerView.measuredWidth / adapter.itemCount
                super.measureChildWithMargins(child, widthUsed, heightUsed)
            }
        }
        recyclerView.adapter = adapter

        on<DisposableHandler>().add(isShareActiveObservable.subscribe { isShareActive -> show(group) })
        on<DisposableHandler>().add(on<GroupHandler>().onGroupUpdated().subscribe { this.show(it) })
        on<DisposableHandler>().add(on<GroupHandler>().onGroupChanged().subscribe { this.show(it) })
        on<DisposableHandler>().add(on<GroupHandler>().onEventChanged().subscribe { g -> show(group) })
        on<DisposableHandler>().add(on<GroupHandler>().onPhoneChanged().subscribe { g -> show(group) })
    }

    private fun show(group: Group?) {
        if (group == null) {
            return
        }

        this.group = group
        val event = on<GroupHandler>().onEventChanged().value
        val phone = on<GroupHandler>().onPhoneChanged().value

        val items = ArrayList<ToolbarItem>()

        if (group.hasPhone()) {
            items.add(ToolbarItem(
                    R.string.messages,
                    R.drawable.ic_message_black_24dp,
                    View.OnClickListener { v -> }
            ))

            items.add(ToolbarItem(
                    R.string.photos,
                    R.drawable.ic_photo_black_24dp,
                    View.OnClickListener { v -> }
            ))

            items.add(ToolbarItem(
                    R.string.groups,
                    R.drawable.ic_person_black_24dp,
                    View.OnClickListener { v -> }
            ))
        }

        if (items.size < 3 && event != null) {
            items.add(ToolbarItem(
                    if (isShareActiveObservable.value!!) R.string.cancel else R.string.share,
                    if (isShareActiveObservable.value!!) R.drawable.ic_close_black_24dp else R.drawable.ic_share_black_24dp,
                    View.OnClickListener { v -> toggleShare() }
            ))
        }

        if (items.size < 3 && (event != null || group.physical)) {
            items.add(ToolbarItem(
                    R.string.show_on_map,
                    R.drawable.ic_my_location_black_24dp,
                    View.OnClickListener { v ->
                        if (event != null)
                            showEventOnMap(event)
                        else
                            showGroupOnMap(group)
                    }
            ))
        }

        if (items.size < 3 && isEventCancelable(event)) {
            items.add(ToolbarItem(
                    R.string.cancel,
                    R.drawable.ic_close_black_24dp,
                    View.OnClickListener { v ->
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
                                }, { error -> on<DefaultAlerts>().thatDidntWork() }))
                            }
                            show()
                        }
                    }
            ))
        }

        if (items.size < 3 && group.physical && isEmptyOrWhitespace(group.name)) {
            items.add(ToolbarItem(
                    R.string.set_name,
                    R.drawable.ic_edit_location_black_24dp,
                    View.OnClickListener { v ->
                        on<PhysicalGroupUpgradeHandler>().convertToHub(group, { updatedGroup ->
                            on<GroupHandler>().showGroupName(updatedGroup)
                            show(updatedGroup)
                        })
                    }
            ))
        }

        if (items.size < 3 && group.physical && isEmptyOrWhitespace(group.photo)) {
            items.add(ToolbarItem(
                    R.string.set_background,
                    R.drawable.ic_camera_black_24dp,
                    View.OnClickListener { v ->
                        on<PhysicalGroupUpgradeHandler>().setBackground(group, { updateGroup ->
                            on<GroupHandler>().setGroupBackground(updateGroup)
                            show(updateGroup)
                        })
                    }
            ))
        }

        if (items.size < 3 && (event != null || group.physical)) {
            items.add(ToolbarItem(
                    R.string.get_directions,
                    R.drawable.ic_directions_black_24dp,
                    View.OnClickListener { v ->
                        on<OutboundHandler>().openDirections(LatLng(
                                group.latitude!!,
                                group.longitude!!
                        ))
                    }
            ))
        }

        if (items.size < 3 && group.physical) {
            items.add(ToolbarItem(
                    R.string.host_event,
                    R.drawable.ic_event_note_black_24dp,
                    View.OnClickListener { v ->
                        on<EventHandler>().createNewEvent(LatLng(
                                group.latitude!!,
                                group.longitude!!
                        ), group.isPublic) {
                            showEventOnMap(it)
                        }
                    }
            ))
        }

        adapter.items = items
    }

    private fun isEventCancelable(event: Event?): Boolean {
        return event != null && on<PersistenceHandler>().phoneId != null &&
                !event.cancelled && event.creator != null &&
                Date().before(event.endsAt) &&
                event.creator == on<PersistenceHandler>().phoneId
    }

    private fun showGroupOnMap(group: Group?) {
        (on<ActivityHandler>().activity as CircularRevealActivity)
                .finish { on<MapActivityHandler>().showGroupOnMap(group!!) }
    }

    private fun toggleShare() {
        if (isShareActiveObservable.value!!) {
            isShareActiveObservable.onNext(false)
            return
        }

        isShareActiveObservable.onNext(true)
    }


    private fun showEventOnMap(event: Event?) {
        (on<ActivityHandler>().activity as CircularRevealActivity)
                .finish { on<MapActivityHandler>().showEventOnMap(event!!) }
    }

    internal class ToolbarItem(@field:StringRes var name: Int, @field:DrawableRes var icon: Int, var onClickListener: View.OnClickListener)
}
