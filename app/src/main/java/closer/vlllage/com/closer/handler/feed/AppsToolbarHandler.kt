package closer.vlllage.com.closer.handler.feed

import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupToolbarHandler
import closer.vlllage.com.closer.handler.group.ToolbarAdapter
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.map.FeedHandler
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject

class AppsToolbarHandler constructor(private val on: On) {
    fun attach(toolbarAdapter: ToolbarAdapter, showCalendarIndicator: BehaviorSubject<Boolean>, isLight: Boolean = false) {
        val appsToolbarOrder = on<PersistenceHandler>().appsToolbarOrder

        toolbarAdapter.isLight = isLight

        toolbarAdapter.items = listOf(
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.welcome),
                        R.drawable.ic_baseline_school_24,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_WELCOME)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_WELCOME,
                        color = R.color.green),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.contacts),
                        R.drawable.ic_person_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_CONTACTS)
                            on<AccountHandler>().updatePrivateOnly(true)
                        },
                        value = ContentViewType.HOME_CONTACTS,
                        color = R.color.colorPrimary),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.map),
                        R.drawable.ic_whatshot_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_POSTS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_POSTS,
                        color = R.color.orange),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.notifications),
                        R.drawable.ic_notifications_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_NOTIFICATIONS)
                        },
                        value = ContentViewType.HOME_NOTIFICATIONS,
                        color = R.color.colorAccent),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.places),
                        R.drawable.ic_location_on_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_PLACES)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_PLACES,
                        color = R.color.purple),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.calendar),
                        R.drawable.ic_event_note_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_CALENDAR)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_CALENDAR,
                        color = R.color.red,
                        indicator = showCalendarIndicator),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.communities),
                        R.drawable.ic_location_city_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_GROUPS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_GROUPS,
                        color = R.color.green),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.quests),
                        R.drawable.ic_baseline_terrain_24,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_QUESTS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_QUESTS,
                        color = R.color.forestgreen),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.things_to_do),
                        R.drawable.ic_beach_access_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_ACTIVITIES)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_ACTIVITIES,
                        color = R.color.colorAccent),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.lifestyles),
                        R.drawable.ic_star_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_LIFESTYLES)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_LIFESTYLES,
                        color = R.color.pink500),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.goals),
                        R.drawable.ic_baseline_flag_24,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_GOALS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_GOALS,
                        color = R.color.pink500),
                GroupToolbarHandler.ToolbarItem(
                        on<ResourcesHandler>().resources.getString(R.string.groups),
                        R.drawable.ic_group_black_24dp,
                        {
                            on<FeedHandler>().show(ContentViewType.HOME_FRIENDS)
                            on<AccountHandler>().updatePrivateOnly(false)
                        },
                        value = ContentViewType.HOME_FRIENDS,
                        color = R.color.colorPrimary)
        ).sortedBy {
            appsToolbarOrder.indexOf(it.value).let {
                if (it == -1) Int.MAX_VALUE else it
            }
        }.toMutableList()
    }
}
