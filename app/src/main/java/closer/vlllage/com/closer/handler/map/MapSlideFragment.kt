package closer.vlllage.com.closer.handler.map

import android.Manifest
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.toLatLng
import closer.vlllage.com.closer.handler.bubble.*
import closer.vlllage.com.closer.handler.data.*
import closer.vlllage.com.closer.handler.event.EventBubbleHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.view.*


class MapSlideFragment : PoolFragment() {

    private var locationPermissionWasDenied: Boolean = false
    private var pendingRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val breakpoint = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.maxFullWidth)
        val feedPeekHeightMinus12dp = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.feedPeekHeightMinus12dp)

        mapLayout.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val w = right - left
            if (mapLayout.marginBottom == 0 && w <= breakpoint) {
                mapLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = feedPeekHeightMinus12dp
                }

                mapLayout.requestLayout()
            } else if (mapLayout.marginBottom != 0 && w > breakpoint) {
                mapLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = 0
                }

                mapLayout.requestLayout()
            }
        }

        on<NetworkConnectionViewHandler>().attach(view.connectionError)
        on<TimerHandler>().postDisposable({ on<SyncHandler>().syncAll() }, 1325)
        on<TimerHandler>().postDisposable({ on<RefreshHandler>().refreshAll() }, 1625)

        on<BubbleHandler>().attach(view.findViewById(R.id.bubbleMapLayer), { mapBubble ->
            on<NavigationHandler>().showProfile(mapBubble.phone!!, mapBubble.view)
        }, { mapBubble, position ->
                on<BubbleHandler>().remove(mapBubble)
                mapBubble.onItemClickListener?.invoke(position)
        }, { mapBubble ->
            val groupId = (mapBubble.tag as Event).groupId

            if (groupId != null) {
                on<GroupActivityTransitionHandler>().showGroupMessages(mapBubble.view, groupId)
            } else {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, { mapBubble ->
            val suggestion = mapBubble.tag as Suggestion
            if (suggestion.id != null) {
                on<ShareActivityTransitionHandler>().shareSuggestion(suggestion.id!!)
            }
        }, { mapBubble ->
            val groupId = (mapBubble.tag as Group).id
            on<GroupActivityTransitionHandler>().showGroupMessages(mapBubble.view, groupId)
        })

        on<MapHandler>().onMapReadyListener = { map ->
            on<PhysicalGroupBubbleHandler>().attach()
            on<SuggestionBubbleHandler>().attach()
            on<BubbleHandler>().attach(map)
        }
        on<MapHandler>().onMapChangedListener = {
            on<BubbleHandler>().update()
            on<MapZoomHandler>().update(on<MapHandler>().zoom)
        }
        on<MapHandler>().onMapClickedListener = { latLng ->
            val anyActionTaken = on<BubbleHandler>().remove { mapBubble -> BubbleType.MENU == mapBubble.type }

            if (!anyActionTaken) {
                showMapMenu(latLng, null)
            }
        }
        on<MapHandler>().onMapIdleListener = { latLng ->
            on<LocalityHelper>().getLocality(latLng) {
                if (it.isNullOrBlank()) {
                    view.searchMap.hint = on<ResourcesHandler>().resources.getString(R.string.search_map_hint)
                } else {
                    view.searchMap.hint = on<ResourcesHandler>().resources.getString(R.string.search_x_hint, it)
                }
            }

            on<DisposableHandler>().add(on<DataHandler>().run {
                getPhonesNear(latLng)
                        .filter { on<AccountHandler>().privateOnly.not() }
                        .map { mapBubbleFrom(it) }.subscribe({ mapBubbles -> on<BubbleHandler>().replace(mapBubbles) }, { networkError(it) })
            })

            on<DisposableHandler>().add(on<ApiHandler>().getSuggestionsNear(latLng).subscribe({ suggestions -> on<SuggestionHandler>().loadAll(suggestions) }, { networkError(it) }))

            on<RefreshHandler>().refreshEvents(latLng)
            on<RefreshHandler>().refreshPhysicalGroups(latLng)
            on<RefreshHandler>().refreshQuests(latLng)

            on<MapZoomHandler>().update(on<MapHandler>().zoom)

            on<MeetHandler>().setLocation(latLng)
        }
        on<MapHandler>().attach(childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        on<MyBubbleHandler>().start()
        on<MyGroupsLayoutHandler>().attach(view.findViewById(R.id.myGroupsLayout))
        on<MyGroupsLayoutHandler>().setContainerView(view.findViewById(R.id.bottomContainer))

        on<KeyboardVisibilityHandler>().attach(view.findViewById(R.id.contentView))

        on<DisposableHandler>().add(on<KeyboardVisibilityHandler>().isKeyboardVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isVisible -> on<MyGroupsLayoutHandler>().showBottomPadding(!isVisible) }, { it.printStackTrace() }))

        val verifiedNumber = on<PersistenceHandler>().isVerified
        on<MyGroupsLayoutActionsHandler>().showVerifyMyNumber(!verifiedNumber)
        if (!verifiedNumber) {
            on<DisposableHandler>().add(on<ApiHandler>().isVerified().subscribe({ verified ->
                on<PersistenceHandler>().isVerified = verified
                on<MyGroupsLayoutActionsHandler>().showVerifyMyNumber(!verified)
            }, { networkError(it) }))
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(activity as FragmentActivity) { instanceIdResult ->
            on<AccountHandler>().updateDeviceToken(instanceIdResult.token)
        }

        if (on<AccountHandler>().active) {
            on<AccountHandler>().updateStatus(on<AccountHandler>().status)
        }

        on<EventBubbleHandler>().attach()
        on<FeedHandler>().attach(view.feed, view.toTheTopLayout)

        view.toTheTop.setOnClickListener {
            on<FeedHandler>().hide()
        }

        on<DisposableHandler>().add(on<MeetHandler>().total
                .subscribe {
            on<MyGroupsLayoutActionsHandler>().showMeetPeople(it > 0 && on<AccountHandler>().privateOnly.not())
        })

        if (pendingRunnable != null) {
            pendingRunnable!!.run()
            pendingRunnable = null
        }

        if("I am a super god being".isNotBlank()) {
            on<DataVisualsHandler>().attach()

            on<DisposableHandler>().add(on<DataHandler>().getRecentlyActivePhones(100).map {
                it.filter { it.latitude != null && it.longitude != null }.map { LatLng(it.latitude!!, it.longitude!!) }
            }.subscribe({ phones ->
                on<DataVisualsHandler>().setPhones(phones)
            }, { }))

            on<DisposableHandler>().add(on<DataHandler>().getRecentlyActiveGroups(100).map {
                it.filter { it.latitude != null && it.longitude != null }.map { LatLng(it.latitude!!, it.longitude!!) }
            }.subscribe({ phones ->
                on<DataVisualsHandler>().setGroups(phones)
            }, { }))
        }

        view.searchMap.doOnLayout {
            on<MapHandler>().setTopPadding(view.searchMap.bottom)
        }

        view.locateMeButton.setOnClickListener {
            if (on<PermissionHandler>().denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                on<AlertHandler>().make().apply {
                    title = on<ResourcesHandler>().resources.getString(R.string.enable_location_permission)
                    message = on<ResourcesHandler>().resources.getString(R.string.enable_location_permission_rationale)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.open_settings)
                    positiveButtonCallback = { on<SystemSettingsHandler>().showSystemSettings() }
                    show()
                }

                return@setOnClickListener
            }

            on<LocationHandler>().getCurrentLocation {
                on<MapHandler>().centerMap(it.toLatLng())
            }
        }

        view.scanInviteButton.setOnClickListener {
            on<ScanQrCodeHandler>().scan()
        }

        view.searchMap.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(view.searchMap.text.toString())
                false
            }

            true
        }
    }

    private fun search(query: String) {
        on<AccountHandler>().updatePrivateOnly(false)
        on<SearchMapHandler>().next(query)
    }

    private fun showMapMenu(latLng: LatLng, title: String?) {
        val menuBubble = MapBubble(latLng, BubbleType.MENU, true, true)
        menuBubble.onItemClickListener = { position ->
            when (position) {
                0 -> on<PhysicalGroupHandler>().createPhysicalGroup(menuBubble.latLng!!, name = menuBubble.status ?: "")
                1 -> on<EventHandler>().createNewEvent(menuBubble.latLng!!, true) {
                    on<MapHandler>().centerMap(LatLng(
                            it.latitude!!,
                            it.longitude!!
                    ))
                }
                2 -> on<ShareHandler>().shareTo(menuBubble.latLng!!) { group ->
                    val success = on<GroupMessageAttachmentHandler>().shareLocation(menuBubble.latLng!!, group, menuBubble.status)

                    if (!success) {
                        on<DefaultAlerts>().thatDidntWork()
                    } else {
                        on<GroupActivityTransitionHandler>().showGroupMessages(menuBubble.view, group.id)
                    }
                }
                3 -> on<SuggestionHandler>().createNewSuggestion(menuBubble.latLng!!)
                4 -> {
                    on<DefaultInput>().show(R.string.add_new_public_place, R.string.enter_place_name, R.string.create_place, inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS, prefill = menuBubble.status) {
                        on<PhysicalGroupHandler>().createPhysicalGroup(menuBubble.latLng!!, isPublic = true, hub = true, name = it)
                    }
                }
                5 -> {
                    on<DefaultInput>().show(R.string.add_new_private_place, R.string.enter_place_name, R.string.create_place, inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS) {
                        on<PhysicalGroupHandler>().createPhysicalGroup(menuBubble.latLng!!, isPublic = false, hub = true, name = it)
                    }
                }
            }
        }
        on<BubbleHandler>().add(menuBubble)
        menuBubble.status = title
        menuBubble.onViewReadyListener = {
            on<MapBubbleMenuView>()
                    .getMenuAdapter(menuBubble)
                    .setMenuItems(
                            (MapBubbleMenuItem(getString(R.string.talk_here), R.drawable.ic_chat_black_18dp, R.color.purple)),
                            (MapBubbleMenuItem(getString(R.string.host_event), R.drawable.ic_event_note_black_18dp, R.color.red)),
                            (MapBubbleMenuItem(getString(R.string.share_location), R.drawable.ic_share_black_18dp, R.color.colorAccent)),
                            (MapBubbleMenuItem(getString(R.string.add_a_suggestion), R.drawable.ic_edit_location_black_18dp, R.color.colorPrimaryLight)),
                            (MapBubbleMenuItem(getString(R.string.add_new_public_place), R.drawable.ic_add_black_18dp, R.color.purple)),
                            (MapBubbleMenuItem(getString(R.string.add_new_private_place), R.drawable.ic_group_add_black_18dp, R.color.colorPrimary)))

            on<MapBubbleMenuView>().setMenuTitle(menuBubble, title ?: on<ResourcesHandler>().resources.getString(R.string.loading_location))

            if (title == null) {
                on<LocalityHelper>().getName(latLng) {
                    menuBubble.status = it ?: on<ResourcesHandler>().resources.getString(R.string.unknown_place)
                    on<MapBubbleMenuView>().setMenuTitle(menuBubble, it ?: on<ResourcesHandler>().resources.getString(R.string.unknown_place))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val locationPermissionGranted = on<PermissionHandler>().has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (locationPermissionGranted) {
            on<LocationHandler>().getCurrentLocation { location -> on<AccountHandler>().updateGeo(LatLng(location.latitude, location.longitude)) }
        }

        val locationPermissionDenied = on<PermissionHandler>().denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        val isNotificationsPaused = on<PersistenceHandler>().isNotificationsPaused
        on<MyGroupsLayoutActionsHandler>().showUnmuteNotifications(isNotificationsPaused)
        on<MyGroupsLayoutActionsHandler>().showSetMyName(on<AccountHandler>().name.isBlank())

        if (locationPermissionGranted && locationPermissionWasDenied) {
            on<MapHandler>().updateMyLocationEnabled()
            on<MapHandler>().locateMe()
        }

        locationPermissionWasDenied = locationPermissionDenied
    }

    private fun mapBubbleFrom(phoneList: List<Phone>): List<MapBubble> {
        val mapBubbles = mutableListOf<MapBubble>()

        for (phone in phoneList) {
            if (phone.latitude == null || phone.longitude == null) {
                continue
            }

            mapBubbles.add(MapBubble(
                    on<LatLngStr>().to(phone.latitude!!, phone.longitude!!),
                    on<NameHandler>().getName(phone),
                    phone.status
            ).apply {
                tag = phone
                this.phone = phone.id
            })
        }

        return mapBubbles
    }

    private fun networkError(throwable: Throwable) {
        throwable.printStackTrace()
        on<ConnectionErrorHandler>().notifyConnectionError()
    }

    fun onBackPressed(): Boolean {
        return false
    }

    fun handleIntent(intent: Intent?, onRequestMapOnScreenListener: (() -> Unit)?) {
        intent ?: return

        on<IntentHandler>().onNewIntent(intent, onRequestMapOnScreenListener)
        if (Intent.ACTION_VIEW == intent.action) {
            on<FeedHandler>().hide()
        } else if (Intent.ACTION_SEND == intent.action) {
            val name = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            val address = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (address != null) {
                on<DisposableHandler>().add(on<PlacesHandler>().findAddress(address, 1)
                        .subscribe({ addresses ->
                            if (addresses.isEmpty()) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                showAddressOnMap(name ?: on<ResourcesHandler>().resources.getString(R.string.shared_location), addresses[0])
                            }
                        }, { networkError(it) }))

            }
        }
    }

    private fun showAddressOnMap(name: String?, address: Address) {
        val latLng = LatLng(address.latitude, address.longitude)
        showMapMenu(latLng, name)
        on<MapHandler>().centerMap(latLng)
    }

    fun post(runnable: Runnable) {
        if (isAdded) {
            runnable.run()
        } else {
            pendingRunnable = runnable
        }
    }
}
