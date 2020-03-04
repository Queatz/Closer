package closer.vlllage.com.closer.handler.feed

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
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.data.LocationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.create_group_modal.view.*
import kotlinx.android.synthetic.main.feed_item_public_groups.view.*
import java.util.*

class PublicGroupFeedItemHandler constructor(private val on: On) {

    private lateinit var searchGroups: EditText
    private lateinit var saySomething: EditText
    private lateinit var saySomethingHeader: TextView
    private lateinit var searchGroupsAdapter: SearchGroupsAdapter
    private lateinit var groupsHeader: TextView
    private lateinit var groupsRecyclerView: RecyclerView
    private lateinit var sendSomethingButton: ImageButton
    private lateinit var peopleContainer: ViewGroup

    private lateinit var itemView: View

    fun attach(itemView: View) {
        this.itemView = itemView
        groupsRecyclerView = itemView.publicGroupsRecyclerView
        groupsHeader = itemView.publicGroupsHeader
        val eventsRecyclerView = itemView.findViewById<RecyclerView>(R.id.publicEventsRecyclerView)
        val hubsRecyclerView = itemView.findViewById<RecyclerView>(R.id.publicHubsRecyclerView)
        val actionRecyclerView = itemView.findViewById<RecyclerView>(R.id.groupActionsRecyclerView)
        val suggestionsRecyclerView = itemView.findViewById<RecyclerView>(R.id.suggestionsRecyclerView)
        val peopleRecyclerView = itemView.findViewById<RecyclerView>(R.id.peopleRecyclerView)
        searchGroups = itemView.searchGroups
        saySomething = itemView.saySomething
        saySomethingHeader = itemView.saySomethingHeader
        sendSomethingButton = itemView.sendSomethingButton
        peopleContainer = itemView.peopleContainer

        on<GroupActionRecyclerViewHandler>().attach(actionRecyclerView, GroupActionAdapter.Layout.PHOTO)

        searchGroupsAdapter = SearchGroupsAdapter(on, true, { group, view -> openGroup(group.id, view) }, { groupName: String -> createGroup(groupName) })
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        groupsRecyclerView.adapter = searchGroupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(
                groupsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        val searchEventsAdapter = SearchGroupsAdapter(on, false, { group, view -> openGroup(group.id, view) }, { groupName: String -> createGroup(groupName) })
        searchEventsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        eventsRecyclerView.adapter = searchEventsAdapter
        eventsRecyclerView.layoutManager = LinearLayoutManager(
                eventsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        val searchHubsAdapter = SearchGroupsAdapter(on, false, { group, view -> openGroup(group.id, view) }, { groupName: String -> createGroup(groupName) })
        searchHubsAdapter.setLayoutResId(R.layout.search_groups_card_item)

        hubsRecyclerView.adapter = searchHubsAdapter
        hubsRecyclerView.layoutManager = LinearLayoutManager(
                hubsRecyclerView.context,
                LinearLayoutManager.HORIZONTAL,
                false
        )

        on<SuggestionsRecyclerViewHandler>().attach(suggestionsRecyclerView)
        on<PeopleRecyclerViewHandler>().attach(peopleRecyclerView)

        searchGroups.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                on<SearchGroupHandler>().showGroupsForQuery(searchGroups.text.toString())
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        on<SearchGroupHandler>().showGroupsForQuery(searchGroups.text.toString())

        on<DisposableHandler>().add(on<SearchGroupHandler>().groups.subscribe { groups ->
            searchGroupsAdapter.setGroups(groups.filter { !it.hasEvent() && !it.physical })

            searchEventsAdapter.setGroups(groups.filter { it.hasEvent() }.also {
                itemView.eventsHeader.visible = it.isNotEmpty()
            })

            searchHubsAdapter.setGroups(groups.filter { it.hub }.also {
                itemView.placesHeader.visible = it.isNotEmpty()
            })
        })

        on<DisposableHandler>().add(on<SearchGroupHandler>().createGroupName.subscribe {
            searchGroupsAdapter.setCreatePublicGroupName(it)
        })

        val cameraPositionCallback = { cameraPosition: CameraPosition ->
            loadGroups(cameraPosition.target)
            loadSuggestions(cameraPosition.target)
            loadPeople(cameraPosition.target)

            on<LocalityHelper>().getLocality(cameraPosition.target!!) {
                saySomething.hint = it?.let {
                    on<ResourcesHandler>().resources.getString(R.string.say_something_in, it)
                } ?:let {
                    on<ResourcesHandler>().resources.getString(R.string.say_something)
                }
            }
        }

        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE).subscribe {
            loadGroups(on<MapHandler>().center!!)
            loadSuggestions(on<MapHandler>().center!!)
            loadPeople(on<MapHandler>().center!!)
            updatePrivateOnly()
        })

        updatePrivateOnly()

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

    private fun updatePrivateOnly() {
        val showPublic = on<AccountHandler>().privateOnly.not()
        saySomethingHeader.visible = showPublic
        saySomething.visible = showPublic
        sendSomethingButton.visible = showPublic
        peopleContainer.visible = showPublic
        groupsHeader.setText(if (showPublic) R.string.groups_around_here else R.string.your_groups)
        itemView.feedText.setText(if (showPublic) R.string.conversations_around_here else R.string.conversations)
        searchGroupsAdapter.showCreateOption(showPublic)
    }

    private fun loadGroups(target: LatLng) {
        val distance = .12f

        val queryBuilder = on<StoreHandler>().store.box(Group::class).query()
                .notNull(Group_.eventId)
                .and()
                .greater(Group_.updated, on<TimeAgo>().oneMonthAgo()).apply {
                    if (on<AccountHandler>().privateOnly) {
                        or()
                        equal(Group_.isPublic, false)
                    } else {
                        or()
                        isNull(Group_.eventId)
                        between(Group_.latitude, target.latitude - distance, target.latitude + distance)
                        between(Group_.longitude, target.longitude - distance, target.longitude + distance)
                    }
                }

        on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortGroups(true))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groups ->
                    on<SearchGroupHandler>().setGroups(groups)
                    showGroupActions(itemView.groupActionsRecyclerView, itemView.thingsToDoHeader, groups)
                    on<TimerHandler>().post(Runnable { groupsRecyclerView.scrollBy(0, 0) })
                })
    }

    private fun loadPeople(latLng: LatLng) {
        val distance = 0.01714 * 7 // 7 miles

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
                    peopleContainer.visible = phones.isNotEmpty() && on<AccountHandler>().privateOnly.not()
                    on<PeopleRecyclerViewHandler>().setPeople(phones)
                })
    }

    private fun loadSuggestions(latLng: LatLng) {
        val distance = .12f

        val queryBuilder = on<StoreHandler>().store.box(Suggestion::class).query()
                .between(Suggestion_.latitude, latLng.latitude - distance, latLng.latitude + distance)
                .and()
                .between(Suggestion_.longitude, latLng.longitude - distance, latLng.longitude + distance)

        on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortSuggestions(latLng))
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { suggestions ->
                    itemView.suggestionsHeader.visible = suggestions.isNotEmpty() && on<AccountHandler>().privateOnly.not()
                    itemView.suggestionsRecyclerView.visible = suggestions.isNotEmpty() && on<AccountHandler>().privateOnly.not()
                    on<SuggestionsRecyclerViewHandler>().setSuggestions(suggestions)
                })
    }

    private fun saySomethingAtMapCenter() {
        on<MapHandler>().center?.let {
            val text = saySomething.text.toString()
            saySomething.setText("")

            val latLng = LatLng(it.latitude, it.longitude)
            val nearestGroup = on<ProximityHandler>().findGroupsNear(latLng, true).firstOrNull()

            if (nearestGroup?.id != null) {
                sendMessage(nearestGroup.id!!, text)
            } else {
                on<PhysicalGroupHandler>().createPhysicalGroup(latLng) {
                    sendMessage(it, text)
                }
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
            groupMessage.time = Date()
            on<SyncHandler>().sync(groupMessage) {
                on<GroupActivityTransitionHandler>().showGroupMessages(null, groupId)
            }
        }
    }

    private fun showGroupActions(groupActionsRecyclerView: RecyclerView, header: View, groups: List<Group>) {
        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupAction::class).query()
                .`in`(GroupAction_.group, groups
                        .filter { it.id != null }
                        .map { it.id }
                        .toTypedArray())
                .sort(on<SortHandler>().sortGroupActions())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .single()
                .observer { groupActions ->
                    header.visible = groupActions.isNotEmpty()
                    groupActionsRecyclerView.visible = groupActions.isNotEmpty()
                    on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                })
    }

    private fun createGroup(groupName: String?) {
        if (groupName.isNullOrBlank()) {
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(R.string.create_public_group)
                layoutResId = R.layout.create_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = {
                    addGroupDescription(it.trim())
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
            addGroupDescription(groupName)
        }
    }

    private fun addGroupDescription(groupName: String) {
        searchGroups.setText("")

        on<LocationHandler>().getCurrentLocation({ location ->
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(R.string.group_as_public, groupName)
                layoutResId = R.layout.create_public_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = { about ->
                    val group = on<StoreHandler>().create(Group::class.java)
                    group!!.name = groupName
                    group.about = about
                    group.isPublic = true
                    group.latitude = location.latitude
                    group.longitude = location.longitude
                    on<StoreHandler>().store.box(Group::class).put(group)
                    on<SyncHandler>().sync(group, { groupId ->
                        openGroup(groupId, null)
                    })
                }
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.create_public_group)
                show()
            }

        }, { on<DefaultAlerts>().thatDidntWork(on<ResourcesHandler>().resources.getString(R.string.location_is_needed)) })
    }

    fun openGroup(groupId: String?, view: View?) {
        on<GroupActivityTransitionHandler>().showGroupMessages(view, groupId)
    }

}
