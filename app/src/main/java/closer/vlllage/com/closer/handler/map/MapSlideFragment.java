package closer.vlllage.com.closer.handler.map;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import closer.vlllage.com.closer.MapsActivity;
import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuItem;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuView;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.event.EventBubbleHandler;
import closer.vlllage.com.closer.handler.event.EventHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.PhysicalGroupBubbleHandler;
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardVisibilityHandler;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.helpers.ViewAttributeHandler;
import closer.vlllage.com.closer.pool.PoolFragment;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Suggestion;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapSlideFragment extends PoolFragment {

    private boolean locationPermissionWasDenied;
    private Runnable pendingRunnable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_maps, container, false);

        $(NetworkConnectionViewHandler.class).attach(view.findViewById(R.id.connectionError));
        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());
        $(TimerHandler.class).postDisposable(() -> $(SyncHandler.class).syncAll(), 1325);
        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(BubbleHandler.class).attach(view.findViewById(R.id.bubbleMapLayer), mapBubble -> {
            if ($(MyBubbleHandler.class).isMyBubble(mapBubble)) {
                $(MapActivityHandler.class).goToScreen(MapsActivity.EXTRA_SCREEN_PERSONAL);
            } else {
                $(ReplyLayoutHandler.class).replyTo(mapBubble);
            }
        }, (mapBubble, position) -> {
            $(BubbleHandler.class).remove(mapBubble);
            if (mapBubble.getOnItemClickListener() != null) {
                mapBubble.getOnItemClickListener().onItemClick(position);
            }
        }, mapBubble -> {
            String groupId = ((Event) mapBubble.getTag()).getGroupId();

            if (groupId != null) {
                $(GroupActivityTransitionHandler.class).showGroupMessages(mapBubble.getView(), groupId);
            } else {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, mapBubble -> {
            $(SuggestionHandler.class).clearSuggestions();
            $(ShareHandler.class).shareTo(mapBubble.getLatLng(), group -> {
                boolean success = false;
                if (mapBubble.getTag() instanceof Suggestion) {
                    Suggestion suggestion = (Suggestion) mapBubble.getTag();
                    success = $(GroupMessageAttachmentHandler.class).shareSuggestion(suggestion, group);
                }

                if (!success) {
                    $(DefaultAlerts.class).thatDidntWork();
                    return;
                }

                $(GroupActivityTransitionHandler.class).showGroupMessages(mapBubble.getView(), group.getId());
            });
        }, mapBubble -> {
            String groupId = ((Group) mapBubble.getTag()).getId();
            $(GroupActivityTransitionHandler.class).showGroupMessages(mapBubble.getView(), groupId);
        });

        $(MapHandler.class).setOnMapReadyListener(map -> $(BubbleHandler.class).attach(map));
        $(MapHandler.class).setOnMapChangedListener(() -> {
            $(BubbleHandler.class).update();
            $(MapZoomHandler.class).update($(MapHandler.class).getZoom());
        });
        $(MapHandler.class).setOnMapClickedListener(latLng -> {
            if ($(ReplyLayoutHandler.class).isVisible()) {
                $(ReplyLayoutHandler.class).showReplyLayout(false);
            } else {
                boolean anyActionTaken;
                anyActionTaken = $(SuggestionHandler.class).clearSuggestions();
                anyActionTaken = anyActionTaken || $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));

                if (!anyActionTaken) {
                    MapBubble menuBubble = new MapBubble(latLng, BubbleType.MENU);
                    menuBubble.setOnItemClickListener(position -> {
                        switch (position) {
                            case 0:
                                $(PhysicalGroupHandler.class).createPhysicalGroup(menuBubble.getLatLng());
                                break;
                            case 1:
                                $(ShareHandler.class).shareTo(menuBubble.getLatLng(), group -> {
                                    boolean success = $(GroupMessageAttachmentHandler.class).shareLocation(menuBubble.getLatLng(), group);

                                    if (!success) {
                                        $(DefaultAlerts.class).thatDidntWork();
                                    } else {
                                        $(GroupActivityTransitionHandler.class).showGroupMessages(menuBubble.getView(), group.getId());
                                    }
                                });
                                break;
                            case 2:
                                $(EventHandler.class).createNewEvent(menuBubble.getLatLng());
                                break;
                        }
                    });
                    $(BubbleHandler.class).add(menuBubble);
                    menuBubble.setOnViewReadyListener(menuBubbleView -> {
                        $(MapBubbleMenuView.class)
                                .getMenuAdapter(menuBubble)
                                .setMenuItems(
                                        new MapBubbleMenuItem().setTitle(getString(R.string.talk_here)).setIconRes(R.drawable.ic_wifi_black_18dp),
                                        new MapBubbleMenuItem().setTitle(getString(R.string.share_this_location)).setIconRes(R.drawable.ic_share_black_18dp),
                                        new MapBubbleMenuItem().setTitle(getString(R.string.add_event_here)).setIconRes(R.drawable.ic_event_note_black_24dp));
                    });
                }
            }
        });
        $(MapHandler.class).setOnMapLongClickedListener(latLng -> {
            $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));

            MapBubble menuBubble = new MapBubble(latLng, BubbleType.MENU);
            menuBubble.setOnItemClickListener(position -> {
                switch (position) {
                    case 0:
                        $(SuggestionHandler.class).createNewSuggestion(menuBubble.getLatLng());
                        break;
                }
            });
            $(BubbleHandler.class).add(menuBubble);
            menuBubble.setOnViewReadyListener(menuBubbleView -> {
                $(MapBubbleMenuView.class)
                        .getMenuAdapter(menuBubble)
                        .setMenuItems(new MapBubbleMenuItem().setTitle(getString(R.string.add_suggestion_here)));
            });
        });
        $(MapHandler.class).setOnMapIdleListener(latLng -> {
            $(DisposableHandler.class).add($(ApiHandler.class).getPhonesNear(latLng).map(this::mapBubbleFrom).subscribe(mapBubbles ->
                    $(BubbleHandler.class).replace(mapBubbles), this::networkError));

            $(DisposableHandler.class).add($(ApiHandler.class).getSuggestionsNear(latLng).map(SuggestionResult::from).subscribe(suggestions ->
                    $(SuggestionHandler.class).loadAll(suggestions), this::networkError));

            $(RefreshHandler.class).refreshEvents(latLng);
            $(RefreshHandler.class).refreshPhysicalGroups(latLng);
        });
        $(MapHandler.class).attach((MapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        $(MyBubbleHandler.class).start();
        $(ReplyLayoutHandler.class).attach(view.findViewById(R.id.replyLayout));
        $(MyGroupsLayoutHandler.class).attach(view.findViewById(R.id.myGroupsLayout));
        $(MyGroupsLayoutHandler.class).setContainerView(view.findViewById(R.id.bottomContainer));

        $(ViewAttributeHandler.class).linkPadding(view.findViewById(R.id.contentAboveView), view.findViewById(R.id.contentView));

        $(KeyboardVisibilityHandler.class).attach(view.findViewById(R.id.contentView));
        $(DisposableHandler.class).add($(KeyboardVisibilityHandler.class).isKeyboardVisible()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isVisible -> $(MyGroupsLayoutHandler.class).showBottomPadding(!isVisible), Throwable::printStackTrace));

        boolean verifiedNumber = $(PersistenceHandler.class).getIsVerified();
        $(MyGroupsLayoutActionsHandler.class).showVerifyMyNumber(!verifiedNumber);
        if (!verifiedNumber) {
            $(DisposableHandler.class).add($(ApiHandler.class).isVerified().subscribe(verified -> {
                $(PersistenceHandler.class).setIsVerified(verified);
                $(MyGroupsLayoutActionsHandler.class).showVerifyMyNumber(!verified);
            }, this::networkError));
        }

        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        if (deviceToken != null) {
            $(AccountHandler.class).updateDeviceToken(deviceToken);
        }

        if ($(AccountHandler.class).getActive()) {
            $(AccountHandler.class).updateStatus($(AccountHandler.class).getStatus());
        }

        $(MyGroupsLayoutActionsHandler.class).showHelpButton(!$(PersistenceHandler.class).getIsHelpHidden());

        $(EventBubbleHandler.class).attach();
        $(PhysicalGroupBubbleHandler.class).attach();
        $(FeedHandler.class).attach(view.findViewById(R.id.feed));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (pendingRunnable != null) {
            pendingRunnable.run();
            pendingRunnable = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean locationPermissionGranted = $(PermissionHandler.class).has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (locationPermissionGranted) {
            $(LocationHandler.class).getCurrentLocation(location -> $(AccountHandler.class).updateGeo(new LatLng(location.getLatitude(), location.getLongitude())));
        }

        boolean locationPermissionDenied = $(PermissionHandler.class).denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        $(MyGroupsLayoutActionsHandler.class).showAllowLocationPermissionsInSettings(locationPermissionDenied);

        boolean isNotificationsPaused = $(PersistenceHandler.class).getIsNotifcationsPaused();
        $(MyGroupsLayoutActionsHandler.class).showUnmuteNotifications(isNotificationsPaused);
        $(MyGroupsLayoutActionsHandler.class).showSetMyName($(Val.class).isEmpty($(AccountHandler.class).getName()));

        if (locationPermissionGranted && locationPermissionWasDenied) {
            $(MapHandler.class).updateMyLocationEnabled();
            $(MapHandler.class).locateMe();
        }

        locationPermissionWasDenied = locationPermissionDenied;
    }


    public List<MapBubble> mapBubbleFrom(List<PhoneResult> phoneResults) {
        List<MapBubble> mapBubbles = new ArrayList<>();

        for (PhoneResult phoneResult : phoneResults) {
            if (phoneResult.geo == null) {
                continue;
            }

            mapBubbles.add(new MapBubble(
                    $(LatLngStr.class).to(phoneResult.geo),
                    phoneResult.name == null ? "" : phoneResult.name,
                    phoneResult.status
            ).setPhone(phoneResult.id));
        }

        return mapBubbles;
    }

    private void networkError(Throwable throwable) {
        throwable.printStackTrace();
        $(ConnectionErrorHandler.class).notifyConnectionError();
    }

    public boolean onBackPressed() {
        if ($(ReplyLayoutHandler.class).isVisible()) {
            $(ReplyLayoutHandler.class).showReplyLayout(false);
            return true;
        }

        return false;
    }

    public void handleIntent(Intent intent, MapViewHandler.OnRequestMapOnScreenListener onRequestMapOnScreenListener) {
        $(IntentHandler.class).onNewIntent(intent, onRequestMapOnScreenListener);
        if (intent != null) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                $(FeedHandler.class).hide();
            } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
                String name = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String address = intent.getStringExtra(Intent.EXTRA_TEXT);

                if (address != null) {
                    $(DisposableHandler.class).add(Single.fromCallable(() -> new Geocoder(getActivity(), Locale.getDefault()).getFromLocationName(address, 1)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(addresses -> {
                        if (addresses.isEmpty()) {
                            $(DefaultAlerts.class).thatDidntWork();
                        } else {
                            showAddressOnMap(name, addresses.get(0));
                        }
                    }, this::networkError));

                }
            }
        }
    }

    private void showAddressOnMap(String name, Address address) {
        Suggestion suggestion = new Suggestion();
        suggestion.setName(name);
        suggestion.setLatitude(address.getLatitude());
        suggestion.setLongitude(address.getLongitude());
        $(MapActivityHandler.class).showSuggestionOnMap(suggestion);
    }

    public void post(Runnable runnable) {
        if (isAdded()) {
            runnable.run();
        } else {
            pendingRunnable = runnable;
        }
    }
}
