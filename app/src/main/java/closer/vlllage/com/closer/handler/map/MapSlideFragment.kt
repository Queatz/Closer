package closer.vlllage.com.closer.handler.map

import android.Manifest
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import closer.vlllage.com.closer.MapsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.*
import closer.vlllage.com.closer.handler.data.*
import closer.vlllage.com.closer.handler.event.EventBubbleHandler
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.group.PhysicalGroupBubbleHandler
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.ProfileHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class MapSlideFragment : PoolFragment() {

    private var locationPermissionWasDenied: Boolean = false
    private var pendingRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        on<NetworkConnectionViewHandler>().attach(view.findViewById(R.id.connectionError))
        on<TimerHandler>().postDisposable(Runnable { on<SyncHandler>().syncAll() }, 1325)
        on<TimerHandler>().postDisposable(Runnable { on<RefreshHandler>().refreshAll() }, 1625)

        on<BubbleHandler>().attach(view.findViewById(R.id.bubbleMapLayer), { mapBubble ->
            if (on<MyBubbleHandler>().isMyBubble(mapBubble)) {
                on<MapActivityHandler>().goToScreen(MapsActivity.EXTRA_SCREEN_PERSONAL)
            } else {
                on<ProfileHandler>().showProfile(mapBubble.phone!!, mapBubble.view)
            }
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
            on<SuggestionHandler>().clearSuggestions()
            on<ShareHandler>().shareTo(mapBubble.latLng!!) { group ->
                var success = false
                if (mapBubble.tag is Suggestion) {
                    val suggestion = mapBubble.tag as Suggestion
                    success = on<GroupMessageAttachmentHandler>().shareSuggestion(suggestion, group)
                }

                if (!success) {
                    on<DefaultAlerts>().thatDidntWork()
                    return@shareTo
                }

                on<GroupActivityTransitionHandler>().showGroupMessages(mapBubble.view, group.id)
            }
        }, { mapBubble ->
            val groupId = (mapBubble.tag as Group).id
            on<GroupActivityTransitionHandler>().showGroupMessages(mapBubble.view, groupId)
        })

        on<MapHandler>().onMapReadyListener = { map ->
            on<PhysicalGroupBubbleHandler>().attach()
            on<BubbleHandler>().attach(map)
        }
        on<MapHandler>().onMapChangedListener = {
            on<BubbleHandler>().update()
            on<MapZoomHandler>().update(on<MapHandler>().zoom)
        }
        on<MapHandler>().onMapClickedListener = { latLng ->
            var anyActionTaken: Boolean
            anyActionTaken = on<SuggestionHandler>().clearSuggestions()
            anyActionTaken = anyActionTaken || on<BubbleHandler>().remove({ mapBubble -> BubbleType.MENU == mapBubble.type })

            if (!anyActionTaken) {
                showMapMenu(latLng, null)
            }
        }
        on<MapHandler>().onMapLongClickedListener = { latLng ->
            on<BubbleHandler>().remove { mapBubble -> BubbleType.MENU == mapBubble.type }

            val menuBubble = MapBubble(latLng, BubbleType.MENU)
            menuBubble.onItemClickListener = { position ->
                when (position) {
                    0 -> on<SuggestionHandler>().createNewSuggestion(menuBubble.latLng!!)
                }
            }
            on<BubbleHandler>().add(menuBubble)
            menuBubble.onViewReadyListener = {
                on<MapBubbleMenuView>()
                        .getMenuAdapter(menuBubble)
                        .setMenuItems(MapBubbleMenuItem(getString(R.string.add_suggestion_here)))
            }
        }
        on<MapHandler>().onMapIdleListener = { latLng ->
            on<DisposableHandler>().add(on<DataHandler>().run {
                getPhonesNear(latLng)
                        .map<List<MapBubble>> { mapBubbleFrom(it) }.subscribe({ mapBubbles -> on<BubbleHandler>().replace(mapBubbles) }, { networkError(it) })
            })

            on<DisposableHandler>().add(on<ApiHandler>().getSuggestionsNear(latLng).subscribe({ suggestions -> on<SuggestionHandler>().loadAll(suggestions) }, { networkError(it) }))

            on<RefreshHandler>().refreshEvents(latLng)
            on<RefreshHandler>().refreshPhysicalGroups(latLng)

            on<MapZoomHandler>().update(on<MapHandler>().zoom)
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
            on<DisposableHandler>().add(on<ApiHandler>().isVerified.subscribe({ verified ->
                on<PersistenceHandler>().isVerified = verified
                on<MyGroupsLayoutActionsHandler>().showVerifyMyNumber(!verified)
            }, { this.networkError(it) }))
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(activity as FragmentActivity) { instanceIdResult ->
            on<AccountHandler>().updateDeviceToken(instanceIdResult.token)
        }

        if (on<AccountHandler>().active) {
            on<AccountHandler>().updateStatus(on<AccountHandler>().status)
        }

        on<MyGroupsLayoutActionsHandler>().showHelpButton(!on<PersistenceHandler>().isHelpHidden)

        on<EventBubbleHandler>().attach()
        on<FeedHandler>().attach(view.findViewById(R.id.feed))

        if (pendingRunnable != null) {
            pendingRunnable!!.run()
            pendingRunnable = null
        }
    }

    private fun showMapMenu(latLng: LatLng, title: String?) {
        val menuBubble = MapBubble(latLng, BubbleType.MENU)
        menuBubble.onItemClickListener = { position ->
            when (position) {
                0 -> on<PhysicalGroupHandler>().createPhysicalGroup(menuBubble.latLng!!)
                1 -> on<ShareHandler>().shareTo(menuBubble.latLng!!) { group ->
                    val success = on<GroupMessageAttachmentHandler>().shareLocation(menuBubble.latLng!!, group)

                    if (!success) {
                        on<DefaultAlerts>().thatDidntWork()
                    } else {
                        on<GroupActivityTransitionHandler>().showGroupMessages(menuBubble.view, group.id)
                    }
                }
                2 -> on<EventHandler>().createNewEvent(menuBubble.latLng!!, true) {
                    on<MapHandler>().centerMap(LatLng(
                            it.latitude!!,
                            it.longitude!!
                    ))
                }
            }
        }
        on<BubbleHandler>().add(menuBubble)
        menuBubble.status = title
        menuBubble.onViewReadyListener = {
            on<MapBubbleMenuView>()
                    .getMenuAdapter(menuBubble)
                    .setMenuItems(
                            (MapBubbleMenuItem(getString(R.string.talk_here), R.drawable.ic_wifi_black_18dp)),
                            (MapBubbleMenuItem(getString(R.string.share_this_location), R.drawable.ic_share_black_18dp)),
                            (MapBubbleMenuItem(getString(R.string.add_event_here), R.drawable.ic_event_note_black_24dp)))

            on<MapBubbleMenuView>().setMenuTitle(menuBubble, title)
        }
    }

    override fun onResume() {
        super.onResume()

        val locationPermissionGranted = on<PermissionHandler>().has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (locationPermissionGranted) {
            on<LocationHandler>().getCurrentLocation { location -> on<AccountHandler>().updateGeo(LatLng(location.latitude, location.longitude)) }
        }

        val locationPermissionDenied = on<PermissionHandler>().denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        on<MyGroupsLayoutActionsHandler>().showAllowLocationPermissionsInSettings(locationPermissionDenied)

        val isNotificationsPaused = on<PersistenceHandler>().isNotificationsPaused
        on<MyGroupsLayoutActionsHandler>().showUnmuteNotifications(isNotificationsPaused)
        on<MyGroupsLayoutActionsHandler>().showSetMyName(on<Val>().isEmpty(on<AccountHandler>().name))

        if (locationPermissionGranted && locationPermissionWasDenied) {
            on<MapHandler>().updateMyLocationEnabled()
            on<MapHandler>().locateMe()
        }

        locationPermissionWasDenied = locationPermissionDenied
    }

    fun mapBubbleFrom(phoneList: List<Phone>): List<MapBubble> {
        val mapBubbles = ArrayList<MapBubble>()

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
                on<DisposableHandler>().add(Single.fromCallable { Geocoder(activity, Locale.getDefault()).getFromLocationName(address, 1) }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ addresses ->
                            if (addresses.isEmpty()) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                showAddressOnMap(name, addresses[0])
                            }
                        }, { this.networkError(it) }))

            }
        }
    }

    private fun showAddressOnMap(name: String, address: Address) {
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
