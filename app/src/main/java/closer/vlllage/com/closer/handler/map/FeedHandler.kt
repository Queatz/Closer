package closer.vlllage.com.closer.handler.map

import android.graphics.Rect
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageHelper
import closer.vlllage.com.closer.handler.group.GroupToolbarHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.kotlin.oneOf
import io.objectbox.kotlin.or
import io.objectbox.query.QueryBuilder
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FeedHandler constructor(private val on: On) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mixedAdapter: MixedHeaderAdapter

    private var groupActionsObservable: DataSubscription? = null
    private var groupActionsQueryString = ""
    private var groupActionsGroups = listOf<Group>()

    fun attach(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        layoutManager = LinearLayoutManager(
                recyclerView.context,
                RecyclerView.VERTICAL,
                false
        )
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = null

        on<DisposableHandler>().add(on<SettingsHandler>()
                .observe(UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME)
                .subscribe {
                    on<LightDarkHandler>().setLight(it)
                })

        mixedAdapter = MixedHeaderAdapter(On(on).apply {
            use<GroupMessageHelper> {
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
                onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group1.id) }
            }
        })

        mixedAdapter.content = on<PersistenceHandler>().lastFeedTab ?: FeedContent.EXPLORE

        recyclerView.adapter = mixedAdapter

        on<DisposableHandler>().add(on<StoreHandler>().store.box(Notification::class).query()
                .sort(on<SortHandler>().sortNotifications())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { setNotifications(it) })

        val distance = .12f

        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE)
                .flatMap {
                    on<MapHandler>().onMapIdleObservable()
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
                    val groupPreviewQueryBuilder = on<StoreHandler>().store.box(Group::class).query().apply {
                        if (!on<AccountHandler>().privateOnly) {
                            between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                                    .and()
                                    .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                        } else {
                            equal(Group_.isPublic, false)
                        }
                    }
                    on<DisposableHandler>().add(groupPreviewQueryBuilder
                            .sort(on<SortHandler>().sortGroups(false))
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer { setGroups(it) })
                })

        on<DisposableHandler>().add(on<KeyboardVisibilityHandler>().isKeyboardVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { visible ->
            if (visible) {
                recyclerView.setPadding(0, 0, 0, on<KeyboardVisibilityHandler>().lastKeyboardHeight)
                recyclerView.requestLayout()
                recyclerView.postInvalidate()
            } else {
                recyclerView.setPadding(0, 0, 0, 0)
                recyclerView.requestLayout()
                recyclerView.postInvalidate()
            }
        })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMessage::class).query()
                .greater(GroupMessage_.updated, on<TimeAgo>().fifteenDaysAgo())
                .sort(on<SortHandler>().sortGroupMessages(true))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { setGroupMessages(it) })
    }

    fun searchGroupActions(queryString: String) {
        groupActionsQueryString = queryString
        setGroupActions(groupActionsGroups)
    }

    private fun setGroups(groups: List<Group>) {
        mixedAdapter.groups = groups.toMutableList()
        setGroupActions(groups)
    }

    private fun setGroupMessages(groupMessages: List<GroupMessage>) {
        mixedAdapter.groupMessages = groupMessages.toMutableList()
    }

    private fun setGroupActions(groups: List<Group>) {
        groupActionsGroups = groups
        groupActionsObservable?.let { on<DisposableHandler>().dispose(it) }

        groupActionsObservable = on<StoreHandler>().store.box(GroupAction::class).query(
                GroupAction_.group.oneOf(groups
                        .filter { it.id != null }
                        .map { it.id }
                        .toTypedArray()
                ).and(
                        GroupAction_.about.contains(groupActionsQueryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                        GroupAction_.name.contains(groupActionsQueryString, QueryBuilder.StringOrder.CASE_INSENSITIVE) or
                        GroupAction_.flow.contains(groupActionsQueryString, QueryBuilder.StringOrder.CASE_INSENSITIVE)
                )
        )
                .sort(on<SortHandler>().sortGroupActions())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupActions ->
                    mixedAdapter.groupActions = groupActions.toMutableList()
                }

        on<DisposableHandler>().add(groupActionsObservable!!)
    }

    private fun setNotifications(notifications: List<Notification>) {
        mixedAdapter.notifications = notifications.toMutableList()
    }

    fun feedContent() = mixedAdapter.content

    fun hide() {
        if (layoutManager.findFirstVisibleItemPosition() > 2) {
            recyclerView.scrollToPosition(2)
        }

        if (layoutManager.findFirstVisibleItemPosition() != 0) {
            recyclerView.smoothScrollToPosition(0)
        } else {
            reveal(false)
        }
    }

    fun show(item: GroupToolbarHandler.ToolbarItem) {
        reveal(true)

        when (item.value) {
            ContentViewType.HOME_NOTIFICATIONS -> FeedContent.NOTIFICATIONS
            ContentViewType.HOME_CALENDAR -> FeedContent.CALENDAR
            ContentViewType.HOME_ACTIVITIES -> FeedContent.ACTIVITIES
            ContentViewType.HOME_GROUPS -> FeedContent.GROUPS
            ContentViewType.HOME_EXPLORE -> FeedContent.EXPLORE
            ContentViewType.HOME_POSTS -> FeedContent.POSTS
            else -> null
        }?.let {
            mixedAdapter.content = it
            on<PersistenceHandler>().lastFeedTab = it
        }
    }

    private fun reveal(show: Boolean) {
        recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.let { feedItemView ->
            val recyclerBounds = Rect().also { recyclerView.getGlobalVisibleRect(it) }
            val feedBounds = Rect().also { feedItemView.getGlobalVisibleRect(it) }
            val scroll = feedBounds.top - (recyclerBounds.top + recyclerBounds.bottom) / 3
            val scrollBuffer = feedBounds.top - (recyclerBounds.top + recyclerBounds.bottom) / 1.5f

            if ((show && scrollBuffer > 0) || (!show && scroll < 0)) {
                recyclerView.smoothScrollBy(0, if (show) scroll else scrollBuffer.toInt())
            }
        }
    }
}
