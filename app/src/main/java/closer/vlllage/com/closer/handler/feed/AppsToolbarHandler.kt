package closer.vlllage.com.closer.handler.feed

import android.view.View
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupToolbarHandler
import closer.vlllage.com.closer.handler.group.ToolbarAdapter
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject

class AppsToolbarHandler constructor(private val on: On) {
    fun attach(toolbarAdapter: ToolbarAdapter, showCalendarIndicator: BehaviorSubject<Boolean>) {
        val appsToolbarOrder = on<PersistenceHandler>().appsToolbarOrder

        toolbarAdapter.items = listOf(
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.contacts),
                        R.drawable.ic_person_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_CONTACTS)
                            on<AccountHandler>().updatePrivateOnly(true)
                        },
                        value = ContentViewType.HOME_CONTACTS,
                        color = R.color.colorPrimary),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.posts),
                        R.drawable.ic_whatshot_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_POSTS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_POSTS,
                        color = R.color.orange),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.notifications),
                        R.drawable.ic_notifications_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_NOTIFICATIONS)
                        },
                        value = ContentViewType.HOME_NOTIFICATIONS,
                        color = R.color.colorAccent),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.places),
                        R.drawable.ic_location_on_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_PLACES)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_PLACES,
                        color = R.color.purple),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.events),
                        R.drawable.ic_event_note_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_CALENDAR)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_CALENDAR,
                        color = R.color.red,
                        indicator = showCalendarIndicator),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.communities),
                        R.drawable.ic_location_city_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_GROUPS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_GROUPS,
                        color = R.color.green),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.quests),
                        R.drawable.ic_baseline_terrain_24,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_QUESTS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_QUESTS,
                        color = R.color.forestgreen),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.things_to_do),
                        R.drawable.ic_beach_access_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_ACTIVITIES)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_ACTIVITIES,
                        color = R.color.colorAccent),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.groups),
                        R.drawable.ic_group_black_24dp,
                        View.OnClickListener {
                            toolbarAdapter.selectedContentView.onNext(ContentViewType.HOME_FRIENDS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_FRIENDS,
                        color = R.color.colorPrimary)
        ).sortedBy {
            appsToolbarOrder.indexOf(it.value)
        }.toMutableList()
    }
}
