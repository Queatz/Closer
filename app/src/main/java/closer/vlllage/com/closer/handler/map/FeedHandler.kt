package closer.vlllage.com.closer.handler.map

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.FilterGroups
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageHelper
import closer.vlllage.com.closer.handler.group.GroupToolbarHandler
import closer.vlllage.com.closer.handler.group.SearchGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class FeedHandler constructor(private val on: On) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mixedAdapter: MixedHeaderAdapter

    private var groupActionsObservable: DataSubscription? = null
    private var questsObservable: DataSubscription? = null
    private var groupMessagesObservable: DataSubscription? = null
    private var lastKnownQueryString = ""
    private var groupActionsGroups = listOf<Group>()
    private var isToTheTopVisible = false
    private var content = BehaviorSubject.create<FeedContent>()
    private var loadGroupsDisposableGroup: DisposableGroup = on<DisposableHandler>().group()

    fun attach(recyclerView: RecyclerView, toTheTop: View) {
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

        mixedAdapter.content = on<PersistenceHandler>().lastFeedTab ?: FeedContent.POSTS

        content.onNext(mixedAdapter.content)

        recyclerView.adapter = mixedAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                val visible = layoutManager.findFirstVisibleItemPosition() >= 3

                if (isToTheTopVisible != visible) {
                    isToTheTopVisible = visible

                    toTheTop.clearAnimation()

                    toTheTop.startAnimation((if (visible) AlphaAnimation(if (toTheTop.visible) toTheTop.alpha else 0f, 1f) else AlphaAnimation(toTheTop.alpha, 0f)).apply {
                        duration = 150

                        if (!visible) {
                            setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {

                                }

                                override fun onAnimationEnd(animation: Animation?) {
                                    toTheTop.visible = false
                                }

                                override fun onAnimationStart(animation: Animation?) {
                                }
                            })
                        }

                        toTheTop.visible = true
                    })
                }
            }
        })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(Notification::class).query()
                .sort(on<SortHandler>().sortNotifications())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { setNotifications(it) })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(Group::class).query(
                Group_.direct.equal(true)
        )
                .sort(on<SortHandler>().sortGroups())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer {
                    mixedAdapter.contacts = it.toMutableList()
                })

        on<DisposableHandler>().add(on<KeyboardVisibilityHandler>().isKeyboardVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { visible ->
                    if (visible) {
                        recyclerView.setPaddingRelative(recyclerView.paddingStart, 0, recyclerView.paddingEnd, on<KeyboardVisibilityHandler>().lastKeyboardHeight)
                        recyclerView.requestLayout()
                        recyclerView.postInvalidate()
                    } else {
                        recyclerView.setPaddingRelative(recyclerView.paddingStart, 0, recyclerView.paddingEnd, 0)
                        recyclerView.requestLayout()
                        recyclerView.postInvalidate()
                    }
                })

        content.distinctUntilChanged().switchMap { content -> on<SearchGroupHandler>().groups.map { Pair(content, it) } }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    setGroups(when (result.first) {
                        FeedContent.GROUPS -> on<FilterGroups>().public(result.second)
                        FeedContent.PLACES -> on<FilterGroups>().hub(result.second)
                        else -> result.second
                    })
                }, {}).also {
                    on<DisposableHandler>().add(it)
                }

        on<DisposableHandler>().add(content.switchMap { on<MapHandler>().onMapIdleObservable() }.subscribe({
            on<MapHandler>().center?.let { center ->
                loadGroups(center)
                loadQuests(center, lastKnownQueryString)
            }
        }, {}))
    }

    fun searchGroupActions(queryString: String) {
        lastKnownQueryString = queryString
        setGroupActions(groupActionsGroups)
    }

    fun setGroups(groups: List<Group>) {
        mixedAdapter.groups = groups.toMutableList()
        setGroupMessages(groups)
        setGroupActions(groups)
    }

    fun searchQuests(queryString: String) {
        lastKnownQueryString = queryString
        loadQuests(on<MapHandler>().center!!, lastKnownQueryString)
    }

    private fun loadQuests(target: LatLng, queryString: String) {
        questsObservable?.cancel()

        on<Search>().quests(target, queryString) { quests ->
            mixedAdapter.quests = quests.toMutableList()
        }.also { questsObservable = it }
    }

    private fun loadGroups(target: LatLng) {
        loadGroupsDisposableGroup.clear()

        val distance = on<HowFar>().about7Miles

        val queryBuilder = when (feedContent()) {
            FeedContent.FRIENDS -> on<StoreHandler>().store.box(Group::class).query(Group_.isPublic.equal(false).and(Group_.direct.equal(false)))
            else -> on<StoreHandler>().store.box(Group::class).query(Group_.isPublic.equal(true)
                    .and(Group_.eventId.isNull)
                    .and(Group_.phoneId.isNull)
                    .and(Group_.ofId.isNull)
                    .and(Group_.updated.greater(on<TimeAgo>().oneMonthAgo(3)))
                    .and(Group_.latitude.between(target.latitude - distance, target.latitude + distance)
                            .and(Group_.longitude.between(target.longitude - distance, target.longitude + distance)))
            )
        }

        queryBuilder
                .sort(on<SortHandler>().sortGroups(true))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groups ->
                    if (feedContent() == FeedContent.FRIENDS) {
                        on<SearchGroupHandler>().setGroups(groups, includeTopics = true)
                    } else {
                        on<Search>().events(target, single = true) { events ->
                            on<StoreHandler>().store.box(Group::class).query(
                                    Group_.id.oneOf(events.map { it.groupId }.toTypedArray())
                            ).build()
                                    .subscribe()
                                    .single()
                                    .on(AndroidScheduler.mainThread())
                                    .observer { eventGroups ->
                                        on<SearchGroupHandler>().setGroups(groups + eventGroups.filter { eventGroup -> groups.all { it.id != eventGroup.id } }, includeTopics = feedContent() == FeedContent.POSTS)
                                    }.also { loadGroupsDisposableGroup.add(it) }
                        }.also { loadGroupsDisposableGroup.add(it) }
                    }
                }.also { loadGroupsDisposableGroup.add(it) }
    }

    private fun setGroupMessages(groups: List<Group>) {
        groupMessagesObservable?.let { on<DisposableHandler>().dispose(it) }

        groupMessagesObservable = on<StoreHandler>().store.box(GroupMessage::class).query()
                .greater(GroupMessage_.created, on<TimeAgo>().weeksAgo())
                .`in`(GroupMessage_.to, groups
                        .map { it.id }
                        .filterNotNull()
                        .toTypedArray())
                .sort(on<SortHandler>().sortGroupMessages(true))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer {
                    mixedAdapter.groupMessages = it.filter { it.attachment?.contains("\"message\":") != true }.toMutableList()
                }

        on<DisposableHandler>().add(groupMessagesObservable!!)
    }

    private fun setGroupActions(groups: List<Group>) {
        groupActionsGroups = groups
        groupActionsObservable?.let { on<DisposableHandler>().dispose(it) }

        groupActionsObservable = on<Search>().groupActions(groupActionsGroups, lastKnownQueryString) { groupActions ->
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
            ContentViewType.HOME_FRIENDS -> FeedContent.FRIENDS
            ContentViewType.HOME_GROUPS -> FeedContent.GROUPS
            ContentViewType.HOME_POSTS -> FeedContent.POSTS
            ContentViewType.HOME_PLACES -> FeedContent.PLACES
            ContentViewType.HOME_QUESTS -> FeedContent.QUESTS
            ContentViewType.HOME_CONTACTS -> FeedContent.CONTACTS
            else -> null
        }?.let {
            content.onNext(it)
            mixedAdapter.content = it
            on<PersistenceHandler>().lastFeedTab = it
        }
    }

    fun scrollTo(itemView: ViewGroup, child: View) {
        performScrollTo(itemView, child)
        on<KeyboardVisibilityHandler>().isKeyboardVisible
                .timeout(1, TimeUnit.SECONDS)
                .filter { it }
                .take(1)
                .subscribe({
            performScrollTo(itemView, child)
        }, {}).also { on<DisposableHandler>().add(it) }
    }

    private fun performScrollTo(itemView: ViewGroup, child: View) {
        recyclerView.stopScroll()
        recyclerView.stopNestedScroll()

        recyclerView.doOnLayout {
            val h = recyclerView.height - on<KeyboardVisibilityHandler>().lastKeyboardHeight
            val offset = itemView.top + child.bottom - h + on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble)

            recyclerView.scrollBy(0, offset)
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
