package closer.vlllage.com.closer.handler.feed

import android.content.res.ColorStateList
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.FeedHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.quest.QuestHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.create_group_modal.view.*
import kotlinx.android.synthetic.main.feed_item_public_groups.view.*
import java.util.*

class PublicGroupFeedItemHandler constructor(private val on: On) {

    private lateinit var searchGroups: EditText
    private lateinit var saySomething: EditText
    private lateinit var saySomethingHeader: TextView
    private lateinit var searchGroupsAdapter: SearchGroupsAdapter
    private lateinit var searchHubsAdapter: SearchGroupsAdapter
    private lateinit var searchEventsAdapter: SearchGroupsAdapter
    private lateinit var actionHeader: TextView
    private lateinit var groupsHeader: TextView
    private lateinit var eventsHeader: TextView
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var hubsRecyclerView: RecyclerView
    private lateinit var actionRecyclerView: RecyclerView
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var peopleRecyclerView: RecyclerView
    private lateinit var sendSomethingButton: ImageButton
    private lateinit var launchGroupButton: ImageButton
    private lateinit var peopleContainer: ViewGroup
    private lateinit var appsToolbar: RecyclerView

    private lateinit var itemView: View
    private lateinit var onToolbarItemSelected: (GroupToolbarHandler.ToolbarItem) -> Unit
    private lateinit var toolbarAdapter: ToolbarAdapter

    private var nearestGroup: Group? = null
    private val state = ViewState()

    private val stateObservable = BehaviorSubject.createDefault(state)
    private val showCalendarIndicator = BehaviorSubject.createDefault(false)

    private var groupActionsDisposable: DataSubscription? = null

    fun attach(itemView: ViewGroup, onToolbarItemSelected: (GroupToolbarHandler.ToolbarItem) -> Unit) {
        this.itemView = itemView
        this.onToolbarItemSelected = onToolbarItemSelected

        groupsRecyclerView = itemView.publicGroupsRecyclerView
        groupsHeader = itemView.publicGroupsHeader
        actionHeader = itemView.thingsToDoHeader
        eventsHeader = itemView.eventsHeader
        eventsRecyclerView = itemView.publicEventsRecyclerView
        hubsRecyclerView = itemView.publicHubsRecyclerView
        actionRecyclerView = itemView.groupActionsRecyclerView
        suggestionsRecyclerView = itemView.suggestionsRecyclerView
        peopleRecyclerView = itemView.peopleRecyclerView
        searchGroups = itemView.searchGroups
        saySomething = itemView.saySomething
        saySomethingHeader = itemView.saySomethingHeader
        sendSomethingButton = itemView.sendSomethingButton
        launchGroupButton = itemView.launchGroupButton
        peopleContainer = itemView.peopleContainer
        appsToolbar = itemView.appsToolbar

        setupAppsToolbar(appsToolbar)

        on<GroupActionRecyclerViewHandler>().attach(actionRecyclerView, GroupActionDisplay.Layout.PHOTO)

        searchGroupsAdapter = SearchGroupsAdapter(on, true, { group, view -> openGroup(group.id, view) }, { groupName: String, isPublic: Boolean -> createGroup(groupName, isPublic) })
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        groupsRecyclerView.adapter = searchGroupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(
                groupsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        searchEventsAdapter = SearchGroupsAdapter(on, false, { group, view -> openGroup(group.id, view) }, { groupName: String, isPublic: Boolean -> createGroup(groupName, isPublic) })
        searchEventsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        eventsRecyclerView.adapter = searchEventsAdapter
        eventsRecyclerView.layoutManager = LinearLayoutManager(
                eventsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        searchHubsAdapter = SearchGroupsAdapter(on, false, { group, view -> openGroup(group.id, view) }, { groupName: String, isPublic: Boolean -> createGroup(groupName, isPublic) })
        searchHubsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        hubsRecyclerView.adapter = searchHubsAdapter
        hubsRecyclerView.layoutManager = LinearLayoutManager(
                hubsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

//        on<SuggestionsRecyclerViewHandler>().attach(suggestionsRecyclerView)
        on<PeopleRecyclerViewHandler>().attach(peopleRecyclerView)

        searchGroups.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                on<FeedHandler>().searchGroupActions(searchGroups.text.toString())
                on<SearchGroupHandler>().showGroupsForQuery(searchGroups.text.toString())
                on<FeedHandler>().searchQuests(searchGroups.text.toString())
            }
        })

        searchGroups.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (searchGroups.hasFocus() && bottom != oldBottom) on<FeedHandler>().scrollTo(itemView, searchGroups)
        }

        searchGroups.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) on<FeedHandler>().scrollTo(itemView, searchGroups)
        }

        searchGroups.setOnClickListener {
            on<FeedHandler>().scrollTo(itemView, searchGroups)
        }

        on<SearchGroupHandler>().showGroupsForQuery(searchGroups.text.toString())

        on<DisposableHandler>().add(on<SearchGroupHandler>().groups.observeOn(AndroidSchedulers.mainThread()).subscribe { groups ->
            searchEventsAdapter.setGroups(on<FilterGroups>().events(groups).also {
                state.hasEvents = it.isNotEmpty()
                stateObservable.onNext(state)
            })

            if (on<FeedHandler>().feedContent() == FeedContent.POSTS) {
                searchGroupsAdapter.setGroups(on<FilterGroups>().physical(groups))
            } else {
                searchGroupsAdapter.setGroups(on<FilterGroups>().public(groups))
            }
            searchHubsAdapter.setGroups(on<FilterGroups>().hub(groups).also {
                state.hasPlaces = it.isNotEmpty()
                stateObservable.onNext(state)
            })

            showGroupActions(groups)
        })

        toolbarAdapter.selectedContentView
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { content ->
                    if ((content == ContentViewType.HOME_POSTS || content == ContentViewType.HOME_NOTIFICATIONS) && searchGroups.text.isNotEmpty()) {
                        searchGroups.setText("")
                    }

                    if (content == ContentViewType.HOME_CONTACTS) {
                        on<RefreshHandler>().refreshDirectGroups()
                    }
                }.also {
                    on<DisposableHandler>().add(it)
                }

        on<DisposableHandler>().add(on<SearchGroupHandler>().createGroupName.subscribe {
            searchGroupsAdapter.setCreatePublicGroupName(it)
        })

        itemView.historyButton.setOnClickListener {
            when (toolbarAdapter.selectedContentView.value) {
                ContentViewType.HOME_QUESTS -> {
                    on<QuestHandler>().createQuest()
                }
                else -> {
                    on<MapHandler>().center?.let { center ->
                        on<AlertHandler>().make().apply {
                            this.title = on<ResourcesHandler>().resources.getString(R.string.perform_archeology)
                            this.message = on<ResourcesHandler>().resources.getString(R.string.perform_archeology_description)
                            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nevermind)
                            positiveButton = on<ResourcesHandler>().resources.getString(R.string.lets_go)
                            positiveButtonCallback = {
                                on<ShareActivityTransitionHandler>().performArcheology(center)
                            }
                            show()
                        }
                    } ?: run {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                }
            }
        }

        val cameraPositionCallback: (CameraPosition) -> Unit = { cameraPosition: CameraPosition ->
            loadSuggestions(cameraPosition.target)
            loadPeople(cameraPosition.target)

            nearestGroup = on<ProximityHandler>().findGroupsNear(cameraPosition.target, true).firstOrNull()
            updateLaunchGroupButton()

            if (nearestGroup != null) {
                on<PhysicalGroupHandler>().physicalGroupName(nearestGroup!!).subscribe({
                    saySomething.hint = on<ResourcesHandler>().resources.getString(R.string.say_something_in, it)
                }, {
                    saySomething.hint = on<ResourcesHandler>().resources.getString(R.string.say_something_in, on<ResourcesHandler>().resources.getString(R.string.unknown))
                }).also {
                    on<DisposableHandler>().add(it)
                }
            } else {
                on<LocalityHelper>().getLocality(cameraPosition.target!!) {
                    saySomething.hint = it?.let {
                        on<ResourcesHandler>().resources.getString(R.string.say_something_in, it)
                    } ?: let {
                        on<ResourcesHandler>().resources.getString(R.string.say_something)
                    }
                }
            }
        }

        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE).subscribe {
            on<MapHandler>().center?.let { center ->
                loadSuggestions(center)
                loadPeople(center)
            }
            state.privateOnly = on<AccountHandler>().privateOnly
            stateObservable.onNext(state)
        })

        on<DisposableHandler>().add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPositionCallback))

        saySomething.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                saySomethingAtMapCenter()
                true
            } else {
                false
            }
        }

        saySomething.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateLaunchGroupButton()
                if (s.isNullOrBlank()) {
                    sendSomethingButton.setImageResource(R.drawable.ic_camera_black_24dp)
                } else {
                    sendSomethingButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        sendSomethingButton.setOnClickListener {
            saySomethingAtMapCenter()
        }
    }

    private fun updateLaunchGroupButton() {
        launchGroupButton.visible = toolbarAdapter.selectedContentView.value == ContentViewType.HOME_POSTS && saySomething.text.isBlank() && nearestGroup != null

        launchGroupButton.setOnClickListener {
            openGroup(nearestGroup?.id, launchGroupButton)
        }
    }

    private fun setupAppsToolbar(appsToolbar: RecyclerView) {
        on<DragDropHandler>().attach(appsToolbar, {
            itemView.appsToolbarContainer.preventScrolling = it
        }) { from, to ->
            toolbarAdapter.moveItem(from, to)
            on<PersistenceHandler>().appsToolbarOrder = toolbarAdapter.items.mapNotNull { it.value }
        }

        toolbarAdapter = ToolbarAdapter(on, onToolbarItemSelected)

        appsToolbar.layoutManager = object : LinearLayoutManager(appsToolbar.context, RecyclerView.HORIZONTAL, false) {
        }

        appsToolbar.adapter = toolbarAdapter

        toolbarAdapter.selectedContentView.onNext(when (on<FeedHandler>().feedContent()) {
            FeedContent.CALENDAR -> ContentViewType.HOME_CALENDAR
            FeedContent.NOTIFICATIONS -> ContentViewType.HOME_NOTIFICATIONS
            FeedContent.GROUPS -> ContentViewType.HOME_GROUPS
            FeedContent.FRIENDS -> ContentViewType.HOME_FRIENDS
            FeedContent.POSTS -> ContentViewType.HOME_POSTS
            FeedContent.ACTIVITIES -> ContentViewType.HOME_ACTIVITIES
            FeedContent.PLACES -> ContentViewType.HOME_PLACES
            FeedContent.QUESTS -> ContentViewType.HOME_QUESTS
            FeedContent.CONTACTS -> ContentViewType.HOME_CONTACTS
        })

        updateViews()

        on<AppsToolbarHandler>().attach(toolbarAdapter, showCalendarIndicator)
    }

    private fun scrollToolbarTo(content: ContentViewType) {
        toolbarAdapter.items.indexOfFirst { it.value == content }.takeIf { it >= 0 }?.let { index ->
            appsToolbar.smoothScrollToPosition(index)
        }
    }

    private fun updateViews() {
        on<DisposableHandler>().apply {
            add(toolbarAdapter.selectedContentView.distinctUntilChanged()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { scrollToolbarTo(it) }
                    .switchMap { content -> stateObservable.map { Pair(content, state) } }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val content = it.first
                        val state = it.second

                        updateLaunchGroupButton()

                        when (content) {
                            ContentViewType.HOME_CONTACTS -> {
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                searchGroups.visible = false
                                itemView.historyButton.visible = false
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                            }
                            ContentViewType.HOME_NOTIFICATIONS -> {
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                searchGroups.visible = false
                                itemView.historyButton.visible = false
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                            }
                            ContentViewType.HOME_POSTS -> {
                                saySomethingHeader.visible = false
                                saySomething.visible = true
                                sendSomethingButton.visible = true
                                peopleContainer.visible = state.hasPeople
                                eventsHeader.visible = false
                                groupsHeader.visible = true
                                groupsHeader.setText(R.string.conversations)
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = true
                                searchGroups.visible = false
                                itemView.historyButton.visible = false
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                                searchGroupsAdapter.showCreateOption(false)
                            }
                            ContentViewType.HOME_ACTIVITIES -> {
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                if (searchGroups.visible.not()) {
                                    searchGroups.visible = true
                                }
                                on<ResourcesHandler>().resources.getString(R.string.search_for_things_to_do).let { hint ->
                                    if (searchGroups.hint != hint) searchGroups.hint = hint
                                }
                                itemView.historyButton.visible = false
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                            }
                            ContentViewType.HOME_CALENDAR -> {
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                eventsRecyclerView.visible = state.hasEvents
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                searchGroups.visible = true
                                searchGroups.hint = on<ResourcesHandler>().resources.getString(R.string.search_events_hint)
                                itemView.historyButton.visible = false
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                            }
                            ContentViewType.HOME_PLACES -> {
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = state.hasPlaces
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                searchGroups.visible = true
                                searchGroups.hint = on<ResourcesHandler>().resources.getString(R.string.search_places)
                                itemView.historyButton.visible = true
                                itemView.historyButton.setImageResource(R.drawable.ic_history_black_24dp)
                                itemView.historyButton.imageTintList = null
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = true
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                itemView.placesHeader.visible = false
                                actionHeader.visible = false
                                itemView.feedText.setText(R.string.conversations)
                                searchGroups.visible = true
                            }
                            ContentViewType.HOME_QUESTS -> {
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = state.hasQuests
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = false
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                searchGroups.visible = true
                                searchGroups.hint = on<ResourcesHandler>().resources.getString(R.string.search_quests)
                                itemView.historyButton.visible = true
                                itemView.historyButton.setImageResource(R.drawable.ic_add_black_24dp)
                                itemView.historyButton.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.forestgreen))
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = false
                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                itemView.placesHeader.visible = false
                                actionHeader.visible = false
                                searchGroups.visible = true
                            }
                            ContentViewType.HOME_GROUPS -> {
                                val explore = content === ContentViewType.HOME_GROUPS
                                eventsRecyclerView.visible = false
                                hubsRecyclerView.visible = false
                                actionRecyclerView.visible = false
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = true
                                eventsHeader.visible = false
                                groupsHeader.visible = false
                                searchGroups.visible = true
                                searchGroups.hint = on<ResourcesHandler>().resources.getString(R.string.search_communities)
                                itemView.historyButton.visible = explore
                                itemView.historyButton.setImageResource(R.drawable.ic_history_black_24dp)
                                itemView.historyButton.imageTintList = null
                                actionHeader.visible = false
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = false
                                itemView.feedText.visible = true

                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                itemView.feedText.setText(R.string.conversations)
                                searchGroupsAdapter.setCreateIsPublic(true)
                                searchGroupsAdapter.showCreateOption(true)
                            }
                            ContentViewType.HOME_FRIENDS -> {
                                val explore = content === ContentViewType.HOME_GROUPS
                                eventsRecyclerView.visible = state.hasEvents
                                hubsRecyclerView.visible = state.hasPlaces
                                actionRecyclerView.visible = state.hasGroupActions
                                suggestionsRecyclerView.visible = false
                                groupsRecyclerView.visible = true
                                eventsHeader.visible = state.hasEvents
                                groupsHeader.visible = true
                                searchGroups.visible = true
                                searchGroups.hint = on<ResourcesHandler>().resources.getString(R.string.search_public_groups_hint)
                                itemView.historyButton.visible = explore
                                itemView.historyButton.setImageResource(R.drawable.ic_history_black_24dp)
                                itemView.historyButton.imageTintList = null
                                actionHeader.visible = state.hasGroupActions
                                itemView.suggestionsHeader.visible = false
                                itemView.placesHeader.visible = state.hasPlaces
                                itemView.feedText.visible = true

                                saySomethingHeader.visible = false
                                saySomething.visible = false
                                sendSomethingButton.visible = false
                                peopleContainer.visible = false
                                itemView.placesHeader.setText(R.string.your_places)
                                actionHeader.setText(R.string.your_things_to_do)
                                eventsHeader.setText(R.string.your_events)
                                groupsHeader.setText(R.string.your_groups)
                                itemView.feedText.setText(R.string.conversations)
                                searchGroupsAdapter.setCreateIsPublic(false)
                                searchGroupsAdapter.showCreateOption(true)
                            }
                        }
                    })
        }
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
                    state.hasPeople = phones.isNotEmpty()
                    stateObservable.onNext(state)
                    on<PeopleRecyclerViewHandler>().setPeople(phones)
                })
    }

    private fun loadSuggestions(latLng: LatLng) {
// disable until we can either add ratings or make them more useful some other way
//        val distance = on<HowFar>().about7Miles
//
//        val queryBuilder = on<StoreHandler>().store.box(Suggestion::class).query()
//                .between(Suggestion_.latitude, latLng.latitude - distance, latLng.latitude + distance)
//                .and()
//                .between(Suggestion_.longitude, latLng.longitude - distance, latLng.longitude + distance)
//
//        on<DisposableHandler>().add(queryBuilder
//                .sort(on<SortHandler>().sortSuggestions(latLng))
//                .build()
//                .subscribe()
//                .on(AndroidScheduler.mainThread())
//                .single()
//                .observer { suggestions ->
//                    state.hasSuggestions = suggestions.isNotEmpty()
//                    stateObservable.onNext(state)
//                    on<SuggestionsRecyclerViewHandler>().setSuggestions(suggestions)
//                })
    }

    private fun saySomethingAtMapCenter() {
        val text = saySomething.text.toString()
        saySomething.setText("")

        if (nearestGroup?.id != null) {
            sendMessage(nearestGroup!!.id!!, text)
        } else {
            on<MapHandler>().center?.let {
                on<PhysicalGroupHandler>().createPhysicalGroup(LatLng(it.latitude, it.longitude)) {
                    sendMessage(it, text)
                }
            } ?: run {
                on<DefaultAlerts>().thatDidntWork()
            }
        }
    }

    private fun sendMessage(groupId: String, text: String) {
        if (text.isBlank()) {
            on<CameraHandler>().showCamera {
                it ?: return@showCamera

                on<PhotoUploadGroupMessageHandler>().upload(it) { photoId ->
                    val success = on<GroupMessageAttachmentHandler>()
                            .sharePhoto(on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId), groupId)

                    if (!success) {
                        on<DefaultAlerts>().thatDidntWork()
                    }

                    on<GroupActivityTransitionHandler>().showGroupMessages(null, groupId)
                }
            }
        } else {
            val groupMessage = GroupMessage()
            groupMessage.text = text
            groupMessage.from = on<PersistenceHandler>().phoneId
            groupMessage.to = groupId
            groupMessage.created = Date()
            on<SyncHandler>().sync(groupMessage) {
                on<GroupActivityTransitionHandler>().showGroupMessages(null, groupId)
            }
        }
    }

    private fun showGroupActions(groups: List<Group>) {
        groupActionsDisposable?.let { on<DisposableHandler>().dispose(it) }

        groupActionsDisposable = on<StoreHandler>().store.box(GroupAction::class).query()
                .`in`(GroupAction_.group, groups
                        .filter { it.id != null }
                        .map { it.id }
                        .toTypedArray())
                .sort(on<SortHandler>().sortGroupActions())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupActions ->
                    state.hasGroupActions = groupActions.isNotEmpty()
                    stateObservable.onNext(state)
                    on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                }.also { on<DisposableHandler>().add(it) }
    }

    private fun createGroup(groupName: String?, isPublic: Boolean) {
        if (groupName.isNullOrBlank()) {
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(if (isPublic) R.string.create_public_group else R.string.add_new_private_group)
                layoutResId = R.layout.create_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = {
                    addGroupDescription(it.trim(), isPublic)
                }
                onAfterViewCreated = { alertConfig, view ->
                    alertConfig.alertResult = view.input
                }
                buttonClickCallback = {
                    (it as EditText).text.isNotBlank()
                }
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.continue_text)
                show()
            }
        } else {
            addGroupDescription(groupName, isPublic)
        }
    }

    private fun addGroupDescription(groupName: String, isPublic: Boolean) {
        searchGroups.setText("")

        on<MapHandler>().center?.let { latLng ->
            on<LocalityHelper>().getLocality(on<MapHandler>().center!!) { locality ->
                on<AlertHandler>().make().apply {
                    title = groupName
                    message = if (isPublic) locality?.let { on<ResourcesHandler>().resources.getString(R.string.public_group_in_x, it) } ?: on<ResourcesHandler>().resources.getString(R.string.public_group) else on<ResourcesHandler>().resources.getString(R.string.private_group)
                    layoutResId = R.layout.create_public_group_modal
                    textViewId = R.id.input
                    onTextViewSubmitCallback = { about ->
                        val group = on<StoreHandler>().create(Group::class.java)
                        group!!.name = groupName
                        group.about = about
                        group.isPublic = isPublic
                        group.latitude = latLng.latitude
                        group.longitude = latLng.longitude
                        on<StoreHandler>().store.box(Group::class).put(group)
                        on<SyncHandler>().sync(group, { groupId ->
                            openGroup(groupId, null)
                        })
                    }
                    positiveButton = on<ResourcesHandler>().resources.getString(if (isPublic) R.string.create_public_group else R.string.create_private_group)
                    show()
                }
            }
        } ?: run { on<DefaultAlerts>().thatDidntWork() }
    }

    private fun openGroup(groupId: String?, view: View?) {
        on<GroupActivityTransitionHandler>().showGroupMessages(view, groupId)
    }

}

class ViewState {
    var hasGroupActions = false
    var hasEvents = false
    var hasPlaces = false
    var hasQuests = false
    var hasSuggestions = false
    var hasPeople = false
    var privateOnly = false
}
