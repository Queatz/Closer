package closer.vlllage.com.closer.handler.map

import android.Manifest
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Suggestion
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
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
        val view = inflater.inflate(R.layout.activity_maps, container, false)

        `$`(NetworkConnectionViewHandler::class.java).attach(view.findViewById(R.id.connectionError))
        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)
        `$`(TimerHandler::class.java).postDisposable(Runnable { `$`(SyncHandler::class.java).syncAll() }, 1325)
        `$`(TimerHandler::class.java).postDisposable(Runnable { `$`(RefreshHandler::class.java).refreshAll() }, 1625)

        `$`(BubbleHandler::class.java).attach(view.findViewById(R.id.bubbleMapLayer), { mapBubble ->
            if (`$`(MyBubbleHandler::class.java).isMyBubble(mapBubble)) {
                `$`(MapActivityHandler::class.java).goToScreen(MapsActivity.EXTRA_SCREEN_PERSONAL)
            } else {
                `$`(ReplyLayoutHandler::class.java).replyTo(mapBubble)
            }
        },  object : MapBubbleMenuView.OnMapBubbleMenuItemClickListener {
            override fun onMenuItemClick(mapBubble: MapBubble, position: Int) {
                `$`(BubbleHandler::class.java).remove(mapBubble)
                if (mapBubble.onItemClickListener != null) {
                    mapBubble.onItemClickListener!!.onItemClick(position)
                }
            }
        }, object : MapBubbleEventView.MapBubbleEventClickListener {
            override fun onEventClick(mapBubble: MapBubble) {
                val groupId = (mapBubble.tag as Event).groupId

                if (groupId != null) {
                    `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(mapBubble.view, groupId)
                } else {
                    `$`(DefaultAlerts::class.java).thatDidntWork()
                }
            }
        }, object : MapBubbleSuggestionView.MapBubbleSuggestionClickListener {
            override fun onSuggestionClick(mapBubble: MapBubble) {
                `$`(SuggestionHandler::class.java).clearSuggestions()
                `$`(ShareHandler::class.java).shareTo(mapBubble.latLng!!, object : ShareHandler.OnGroupSelectedListener {
                    override fun onGroupSelected(group: Group) {
                        var success = false
                        if (mapBubble.tag is Suggestion) {
                            val suggestion = mapBubble.tag as Suggestion
                            success = `$`(GroupMessageAttachmentHandler::class.java).shareSuggestion(suggestion, group)
                        }

                        if (!success) {
                            `$`(DefaultAlerts::class.java).thatDidntWork()
                            return
                        }

                        `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(mapBubble.view, group.id)
                    }
                })
            }
        }, object : MapBubblePhysicalGroupView.MapBubblePhysicalGroupClickListener {
            override fun onPhysicalGroupClick(mapBubble: MapBubble) {
                val groupId = (mapBubble.tag as Group).id
                `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(mapBubble.view, groupId)
            }
        })


        `$`(MapHandler::class.java).setOnMapReadyListener(object : MapHandler.OnMapReadyListener {
            override fun onMapReady(map: GoogleMap) {
                `$`(PhysicalGroupBubbleHandler::class.java).attach()
                `$`(BubbleHandler::class.java).attach(map)
            }
        })
        `$`(MapHandler::class.java).setOnMapChangedListener(object : MapHandler.OnMapChangedListener {
            override fun onMapChanged() {
                `$`(BubbleHandler::class.java).update()
                `$`(MapZoomHandler::class.java).update(`$`(MapHandler::class.java).zoom)
            }
        })
        `$`(MapHandler::class.java).setOnMapClickedListener(object : MapHandler.OnMapClickedListener {
            override fun onMapClicked(latLng: LatLng) {
                if (`$`(ReplyLayoutHandler::class.java).isVisible) {
                    `$`(ReplyLayoutHandler::class.java).showReplyLayout(false)
                } else {
                    var anyActionTaken: Boolean
                    anyActionTaken = `$`(SuggestionHandler::class.java).clearSuggestions()
                    anyActionTaken = anyActionTaken || `$`(BubbleHandler::class.java).remove({ mapBubble -> BubbleType.MENU == mapBubble.type })

                    if (!anyActionTaken) {
                        showMapMenu(latLng, null)
                    }
                }
            }
        })
        `$`(MapHandler::class.java).setOnMapLongClickedListener(object : MapHandler.OnMapLongClickedListener {
            override fun onMapLongClicked(latLng: LatLng) {
                `$`(BubbleHandler::class.java).remove { mapBubble -> BubbleType.MENU == mapBubble.type }

                val menuBubble = MapBubble(latLng, BubbleType.MENU)
                menuBubble.onItemClickListener = object : MapBubble.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        when (position) {
                            0 -> `$`(SuggestionHandler::class.java).createNewSuggestion(menuBubble.latLng!!)
                        }
                    }
                }
                `$`(BubbleHandler::class.java).add(menuBubble)
                menuBubble.onViewReadyListener = object : MapBubble.OnViewReadyListener {
                    override fun onViewReady(view: View) {
                        `$`(MapBubbleMenuView::class.java)
                                .getMenuAdapter(menuBubble)
                                .setMenuItems(MapBubbleMenuItem(getString(R.string.add_suggestion_here)))
                    }
                }
            }
        })
        `$`(MapHandler::class.java).setOnMapIdleListener(object : MapHandler.OnMapIdleListener {
            override fun onMapIdle(latLng: LatLng) {
                `$`(DisposableHandler::class.java).add(`$`(DataHandler::class.java).getPhonesNear(latLng)
                        .map<List<MapBubble>> { mapBubbleFrom(it) }.subscribe({ mapBubbles -> `$`(BubbleHandler::class.java).replace(mapBubbles) }, { networkError(it) }))

                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).getSuggestionsNear(latLng).subscribe({ suggestions -> `$`(SuggestionHandler::class.java).loadAll(suggestions) }, { networkError(it) }))

                `$`(RefreshHandler::class.java).refreshEvents(latLng)
                `$`(RefreshHandler::class.java).refreshPhysicalGroups(latLng)

                `$`(MapZoomHandler::class.java).update(`$`(MapHandler::class.java).zoom)
            }
        })
        `$`(MapHandler::class.java).attach(childFragmentManager.findFragmentById(R.id.map) as MapFragment)
        `$`(MyBubbleHandler::class.java).start()
        `$`(ReplyLayoutHandler::class.java).attach(view.findViewById(R.id.replyLayout))
        `$`(MyGroupsLayoutHandler::class.java).attach(view.findViewById(R.id.myGroupsLayout))
        `$`(MyGroupsLayoutHandler::class.java).setContainerView(view.findViewById(R.id.bottomContainer))

        `$`(ViewAttributeHandler::class.java).linkPadding(view.findViewById(R.id.contentAboveView), view.findViewById(R.id.contentView))

        `$`(KeyboardVisibilityHandler::class.java).attach(view.findViewById(R.id.contentView))
        `$`(DisposableHandler::class.java).add(`$`(KeyboardVisibilityHandler::class.java).isKeyboardVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ isVisible -> `$`(MyGroupsLayoutHandler::class.java).showBottomPadding(!isVisible) }, { it.printStackTrace() }))

        val verifiedNumber = `$`(PersistenceHandler::class.java).isVerified
        `$`(MyGroupsLayoutActionsHandler::class.java).showVerifyMyNumber(!verifiedNumber)
        if (!verifiedNumber) {
            `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).isVerified.subscribe({ verified ->
                `$`(PersistenceHandler::class.java).isVerified = verified
                `$`(MyGroupsLayoutActionsHandler::class.java).showVerifyMyNumber(!verified)
            }, { this.networkError(it) }))
        }

        val deviceToken = FirebaseInstanceId.getInstance().token

        if (deviceToken != null) {
            `$`(AccountHandler::class.java).updateDeviceToken(deviceToken)
        }

        if (`$`(AccountHandler::class.java).active) {
            `$`(AccountHandler::class.java).updateStatus(`$`(AccountHandler::class.java).status)
        }

        `$`(MyGroupsLayoutActionsHandler::class.java).showHelpButton(!`$`(PersistenceHandler::class.java).isHelpHidden)

        `$`(EventBubbleHandler::class.java).attach()
        `$`(FeedHandler::class.java).attach(view.findViewById(R.id.feed))

        return view
    }

    private fun showMapMenu(latLng: LatLng, title: String?) {
        val menuBubble = MapBubble(latLng, BubbleType.MENU)
        menuBubble.onItemClickListener = object : MapBubble.OnItemClickListener {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> `$`(PhysicalGroupHandler::class.java).createPhysicalGroup(menuBubble.latLng!!)
                    1 -> `$`(ShareHandler::class.java).shareTo(menuBubble.latLng!!, object : ShareHandler.OnGroupSelectedListener {
                        override fun onGroupSelected(group: Group) {
                            val success = `$`(GroupMessageAttachmentHandler::class.java).shareLocation(menuBubble.latLng!!, group)

                            if (!success) {
                                `$`(DefaultAlerts::class.java).thatDidntWork()
                            } else {
                                `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(menuBubble.view, group.id)
                            }
                        }
                    })
                    2 -> `$`(EventHandler::class.java).createNewEvent(menuBubble.latLng!!, true, object : EventHandler.OnEventCreatedListener {
                        override fun onEventCreated(event: Event) {
                            `$`(MapHandler::class.java).centerMap(LatLng(
                                    event.latitude!!,
                                    event.longitude!!
                            ))
                        }
                    })
                }
            }
        }
        `$`(BubbleHandler::class.java).add(menuBubble)
        menuBubble.status = title
        menuBubble.onViewReadyListener = object : MapBubble.OnViewReadyListener {
            override fun onViewReady(view: View) {
                `$`(MapBubbleMenuView::class.java)
                        .getMenuAdapter(menuBubble)
                        .setMenuItems(
                                (MapBubbleMenuItem(getString(R.string.talk_here), R.drawable.ic_wifi_black_18dp)),
                                (MapBubbleMenuItem(getString(R.string.share_this_location), R.drawable.ic_share_black_18dp)),
                                (MapBubbleMenuItem(getString(R.string.add_event_here), R.drawable.ic_event_note_black_24dp)))

                `$`(MapBubbleMenuView::class.java).setMenuTitle(menuBubble, title)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (pendingRunnable != null) {
            pendingRunnable!!.run()
            pendingRunnable = null
        }
    }

    override fun onResume() {
        super.onResume()

        val locationPermissionGranted = `$`(PermissionHandler::class.java).has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (locationPermissionGranted) {
            `$`(LocationHandler::class.java).getCurrentLocation { location -> `$`(AccountHandler::class.java).updateGeo(LatLng(location.latitude, location.longitude)) }
        }

        val locationPermissionDenied = `$`(PermissionHandler::class.java).denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        `$`(MyGroupsLayoutActionsHandler::class.java).showAllowLocationPermissionsInSettings(locationPermissionDenied)

        val isNotificationsPaused = `$`(PersistenceHandler::class.java).isNotificationsPaused
        `$`(MyGroupsLayoutActionsHandler::class.java).showUnmuteNotifications(isNotificationsPaused)
        `$`(MyGroupsLayoutActionsHandler::class.java).showSetMyName(`$`(Val::class.java).isEmpty(`$`(AccountHandler::class.java).name))

        if (locationPermissionGranted && locationPermissionWasDenied) {
            `$`(MapHandler::class.java).updateMyLocationEnabled()
            `$`(MapHandler::class.java).locateMe()
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
                    `$`(LatLngStr::class.java).to(phone.latitude, phone.longitude),
                    `$`(NameHandler::class.java).getName(phone),
                    phone.status!!
            ).apply {
                tag = phone
                this.phone = phone.id
            })
        }

        return mapBubbles
    }

    private fun networkError(throwable: Throwable) {
        throwable.printStackTrace()
        `$`(ConnectionErrorHandler::class.java).notifyConnectionError()
    }

    fun onBackPressed(): Boolean {
        if (`$`(ReplyLayoutHandler::class.java).isVisible) {
            `$`(ReplyLayoutHandler::class.java).showReplyLayout(false)
            return true
        }

        return false
    }

    fun handleIntent(intent: Intent?, onRequestMapOnScreenListener: MapViewHandler.OnRequestMapOnScreenListener) {
        intent ?: return

        `$`(IntentHandler::class.java).onNewIntent(intent, onRequestMapOnScreenListener)
        if (Intent.ACTION_VIEW == intent.action) {
            `$`(FeedHandler::class.java).hide()
        } else if (Intent.ACTION_SEND == intent.action) {
            val name = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            val address = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (address != null) {
                `$`(DisposableHandler::class.java).add(Single.fromCallable { Geocoder(activity, Locale.getDefault()).getFromLocationName(address, 1) }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ addresses ->
                            if (addresses.isEmpty()) {
                                `$`(DefaultAlerts::class.java).thatDidntWork()
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
        `$`(MapHandler::class.java).centerMap(latLng)
    }

    fun post(runnable: Runnable) {
        if (isAdded) {
            runnable.run()
        } else {
            pendingRunnable = runnable
        }
    }
}
