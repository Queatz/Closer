package closer.vlllage.com.closer.handler.map

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.StoryResult
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.FeedVisibilityHandler
import closer.vlllage.com.closer.handler.feed.FilterGroups
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageHelper
import closer.vlllage.com.closer.handler.group.HideHandler
import closer.vlllage.com.closer.handler.group.SearchGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.handler.story.StoryHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class FeedHandler constructor(private val on: On) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mixedAdapter: MixedHeaderAdapter

    private var groupActionsObservable: DataSubscription? = null
    private var questsObservable: DataSubscription? = null
    private var storiesObservable: Disposable? = null
    private var groupMessagesObservable: DataSubscription? = null
    private var lastKnownQueryString = ""
    private var groupActionsGroups = listOf<Group>()
    private var lifestyleAndGoalPhones = listOf<Phone>()
    private var isToTheTopVisible = false
    private var isFirstLoad = true
    private var loadGroupsDisposableGroup: DisposableGroup = on<DisposableHandler>().group()
    private val snapHelper = object : PagerSnapHelper() {

        private var targetPosition: Int = RecyclerView.NO_POSITION
        val noSnap get() = layoutManager.findFirstVisibleItemPosition() < 1 && abs(recyclerView.computeVerticalScrollOffset()) < mixedAdapter.headerMargin

        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            return if (noSnap && velocityY < 0) {
                false
            } else {
                super.onFling(velocityX, velocityY)
            }
        }

        override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
            val result = super.findTargetSnapPosition(layoutManager, velocityX, velocityY)

            targetPosition = if (result == 0)
                RecyclerView.NO_POSITION
            else
                result

            return targetPosition
        }

        override fun calculateDistanceToFinalSnap(layoutManager_: RecyclerView.LayoutManager, targetView: View): IntArray {
            val result = super.calculateDistanceToFinalSnap(layoutManager, targetView)!!

            return if (targetPosition == RecyclerView.NO_POSITION && noSnap) {
                IntArray(2)
            } else {
                result
            }
        }
    }

    var content = BehaviorSubject.create<FeedContent>()

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

        if (on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_REMEMBER_LAST_TAB]) {
            mixedAdapter.content = on<PersistenceHandler>().lastFeedTab ?: FeedContent.POSTS
        } else {
            mixedAdapter.content = FeedContent.POSTS
        }

        content.onNext(mixedAdapter.content)

        on<DisposableHandler>().add(content.observeOn(AndroidSchedulers.mainThread()).subscribe {
            snapHelper.attachToRecyclerView(when (it) {
                FeedContent.POSTS -> recyclerView
                else -> null
            })
        })

        recyclerView.adapter = mixedAdapter

        recyclerView.clearOnScrollListeners()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    (layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()).forEach {
                        on<FeedVisibilityHandler>().onScreen(it)
                    }
                }

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
                    mixedAdapter.contacts = it.filter { it.id == null || !on<HideHandler>().isHidden(it.id!!) }.toMutableList()
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
                        FeedContent.QUESTS -> on<FilterGroups>().quests(result.second)
                        else -> result.second
                    })
                }, {}).also {
                    on<DisposableHandler>().add(it)
                }

        on<DisposableHandler>().add(content.switchMap { on<MapHandler>().onMapIdleObservable() }.subscribe({
            on<MapHandler>().center?.let { center ->
                loadGroups(center)
                loadPeople(center)
                loadStories(center)
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
        loadLifestylesAndGoals()
    }

    private fun loadQuests(target: LatLng, queryString: String) {
        questsObservable?.cancel()

        questsObservable = on<Search>().quests(target, queryString) { quests ->
            mixedAdapter.quests = quests.toMutableList()
        }
    }

    private fun loadStories(target: LatLng) {
        mixedAdapter.loadingStories = true
        storiesObservable?.dispose()
        storiesObservable = on<StoryHandler>().changes.switchMapSingle { on<ApiHandler>().getStoriesNear(target) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mixedAdapter.loadingStories = false
                    mixedAdapter.stories = it
                            .map { StoryResult.from(on, it) }
                            .toMutableList()
                }, {
                    mixedAdapter.loadingStories = false
                    on<ConnectionErrorHandler>().notifyConnectionError()
                })
        }

    private fun loadPeople(latLng: LatLng) {
        val distance = on<HowFar>().about7Miles

        val queryBuilder = on<StoreHandler>().store.box(Phone::class).query()
                .between(Phone_.latitude, latLng.latitude - distance, latLng.latitude + distance)
                .and()
                .between(Phone_.longitude, latLng.longitude - distance, latLng.longitude + distance)
                .and()
                .greater(Phone_.updated, on<TimeAgo>().fifteenDaysAgo())
                .notEqual(Phone_.id, on<PersistenceHandler>().phoneId ?: "")

        on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortPhones())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { phones ->
                    loadLifestylesAndGoals(phones)
                })
    }

    private fun loadLifestylesAndGoals(phones: List<Phone>? = null) {
        phones?.let { lifestyleAndGoalPhones = it }

        val lifestyles = lifestyleAndGoalPhones.map { it.lifestyles ?: listOf() }.flatMap { it }.toTypedArray()
        val goals = lifestyleAndGoalPhones.map { it.goals ?: listOf() }.flatMap { it }.toTypedArray()

        on<DisposableHandler>().add(on<StoreHandler>().store.box(Lifestyle::class).query(Lifestyle_.name.oneOf(lifestyles).let { query ->
            lastKnownQueryString.takeIf { it.isNotBlank() }?.let {
                query.and(Lifestyle_.name.contains(lastKnownQueryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
            } ?: query
        })
                .sort(on<SortHandler>().sortLifestyles())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { lifestyles ->
                    mixedAdapter.lifestyles = lifestyles
                })

        on<DisposableHandler>().add(on<StoreHandler>().store.box(Goal::class).query(Goal_.name.oneOf(goals).let { query ->
            lastKnownQueryString.takeIf { it.isNotBlank() }?.let {
                query.and(Goal_.name.contains(lastKnownQueryString, QueryBuilder.StringOrder.CASE_INSENSITIVE))
            } ?: query
        })
                .sort(on<SortHandler>().sortGoals())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { goals ->
                    mixedAdapter.goals = goals
                })
    }

    private fun loadGroups(target: LatLng) {
        loadGroupsDisposableGroup.clear()

        val distance = on<HowFar>().about7Miles

        val queryBuilder = when (feedContent()) {
            FeedContent.FRIENDS -> on<StoreHandler>().store.box(Group::class).query(Group_.isPublic.equal(false).and(Group_.direct.equal(false).and(Group_.eventId.isNull)
                    .and(Group_.phoneId.isNull)
                    .and(Group_.ofId.isNull)))
            else -> on<StoreHandler>().store.box(Group::class).query(Group_.isPublic.equal(true)
                    .and(Group_.eventId.isNull)
                    .and(Group_.direct.equal(false))
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
                .observer { groups ->
                    when (feedContent()) {
                        FeedContent.FRIENDS -> {
                            on<SearchGroupHandler>().setGroups(groups, includeTopics = true)
                        }
                        FeedContent.QUESTS -> {
                            on<Search>().quests(target) { quests ->
                                on<StoreHandler>().store.box(Group::class).query(
                                        Group_.id.oneOf(quests.map { it.groupId }.toTypedArray())
                                ).build()
                                        .subscribe()
                                        .single()
                                        .on(AndroidScheduler.mainThread())
                                        .observer { questGroups ->
                                            on<SearchGroupHandler>().setGroups(groups + questGroups
                                                    .filter { questGroup -> groups.all { it.id != questGroup.id } }
                                                    .sortedBy { group -> quests.indexOfFirst { quest -> quest.groupId == group.id } })
                                        }.also { loadGroupsDisposableGroup.add(it) }
                            }.also { loadGroupsDisposableGroup.add(it) }
                        }
                        else -> {
                            on<Search>().events(target, single = true) { events ->
                                on<StoreHandler>().store.box(Group::class).query(
                                        Group_.id.oneOf(events.map { it.groupId }.toTypedArray())
                                ).build()
                                        .subscribe()
                                        .single()
                                        .on(AndroidScheduler.mainThread())
                                        .observer { eventGroups ->
                                            on<SearchGroupHandler>().setGroups(groups + eventGroups
                                                    .filter { eventGroup -> groups.all { it.id != eventGroup.id } }
                                                    .sortedBy { group -> events.indexOfFirst { event -> event.groupId == group.id } }, includeTopics = feedContent() == FeedContent.POSTS)
                                        }.also { loadGroupsDisposableGroup.add(it) }
                            }.also { loadGroupsDisposableGroup.add(it) }
                        }
                    }

                    if (isFirstLoad && on<SettingsHandler>()[UserLocalSetting.CLOSER_SETTINGS_OPEN_FEED_EXPANDED]) {
                        isFirstLoad = false
                        on<TimerHandler>().postDisposable({
                            reveal(true)
                        }, 250)
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

    fun show(show: ContentViewType) {
        reveal(true)

        when (show) {
            ContentViewType.HOME_NOTIFICATIONS -> FeedContent.NOTIFICATIONS
            ContentViewType.HOME_CALENDAR -> FeedContent.CALENDAR
            ContentViewType.HOME_ACTIVITIES -> FeedContent.ACTIVITIES
            ContentViewType.HOME_FRIENDS -> FeedContent.FRIENDS
            ContentViewType.HOME_GROUPS -> FeedContent.GROUPS
            ContentViewType.HOME_POSTS -> FeedContent.POSTS
            ContentViewType.HOME_PLACES -> FeedContent.PLACES
            ContentViewType.HOME_QUESTS -> FeedContent.QUESTS
            ContentViewType.HOME_CONTACTS -> FeedContent.CONTACTS
            ContentViewType.HOME_LIFESTYLES -> FeedContent.LIFESTYLES
            ContentViewType.HOME_GOALS -> FeedContent.GOALS
            ContentViewType.HOME_WELCOME -> FeedContent.WELCOME
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
