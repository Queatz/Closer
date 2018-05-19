package closer.vlllage.com.closer;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.group.PhysicalGroupBubbleHandler;
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.event.EventBubbleHandler;
import closer.vlllage.com.closer.handler.event.EventHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;
import closer.vlllage.com.closer.handler.map.IntentHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.handler.map.MyBubbleHandler;
import closer.vlllage.com.closer.handler.map.MyGroupsLayoutHandler;
import closer.vlllage.com.closer.handler.data.PermissionHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.map.ReplyLayoutHandler;
import closer.vlllage.com.closer.handler.search.SearchActivityHandler;
import closer.vlllage.com.closer.handler.map.SetNameHandler;
import closer.vlllage.com.closer.handler.map.ShareHandler;
import closer.vlllage.com.closer.handler.map.StatusLayoutHandler;
import closer.vlllage.com.closer.handler.map.SuggestionHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.TimerHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuView;
import closer.vlllage.com.closer.pool.PoolActivity;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Suggestion;

public class MapsActivity extends PoolActivity {

    public static final String EXTRA_LAT_LNG = "latLng";
    public static final String EXTRA_SUGGESTION = "suggestion";
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_MESSAGE = "message";
    private boolean locationPermissionWasDenied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());
        $(TimerHandler.class).postDisposable(() -> $(SyncHandler.class).syncAll(), 1325);
        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(BubbleHandler.class).attach(findViewById(R.id.bubbleMapLayer), mapBubble -> {
            if ($(MyBubbleHandler.class).isMyBubble(mapBubble)) {
                $(SetNameHandler.class).modifyName();
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
        $(MapHandler.class).setOnMapChangedListener($(BubbleHandler.class)::update);
        $(MapHandler.class).setOnMapClickedListener(latLng -> {
            if ($(ReplyLayoutHandler.class).isVisible()) {
                $(ReplyLayoutHandler.class).showReplyLayout(false);
            } else {
                $(SuggestionHandler.class).clearSuggestions();
                $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));
            }
        });
        $(MapHandler.class).setOnMapLongClickedListener(latLng -> {
            $(BubbleHandler.class).remove(mapBubble -> BubbleType.MENU.equals(mapBubble.getType()));

            MapBubble menuBubble = new MapBubble(latLng, BubbleType.MENU);
            menuBubble.setOnItemClickListener(position -> {
                switch (position) {
                    case 0:
                        $(PhysicalGroupHandler.class).createPhysicalGroup(menuBubble.getLatLng());
                        break;
                    case 1:
                        $(EventHandler.class).createNewEvent(menuBubble.getLatLng());
                        break;
                    case 2:
                        $(ShareHandler.class).shareTo(menuBubble.getLatLng(), group -> {
                            boolean success = $(GroupMessageAttachmentHandler.class).shareLocation(menuBubble.getLatLng(), group);

                            if (!success) {
                                $(DefaultAlerts.class).thatDidntWork();
                            } else {
                                $(GroupActivityTransitionHandler.class).showGroupMessages(menuBubble.getView(), group.getId());
                            }
                        });
                        break;
                    case 3:
                        $(SuggestionHandler.class).createNewSuggestion(menuBubble.getLatLng());
                        break;
                }
            });
            $(BubbleHandler.class).add(menuBubble);
            $(MapBubbleMenuView.class)
                    .getMenuAdapter(menuBubble)
                    .setMenuItems(getString(R.string.talk_here),
                            getString(R.string.add_event_here),
                            getString(R.string.share_this_location),
                            getString(R.string.add_suggestion_here));
        });
        $(MapHandler.class).setOnMapIdleListener(latLng -> {
            $(DisposableHandler.class).add($(ApiHandler.class).getPhonesNear(latLng).map(this::mapBubbleFrom).subscribe(mapBubbles ->
                            $(BubbleHandler.class).replace(mapBubbles), this::networkError));

            $(DisposableHandler.class).add($(ApiHandler.class).getSuggestionsNear(latLng).map(SuggestionResult::from).subscribe(suggestions ->
                            $(SuggestionHandler.class).loadAll(suggestions), this::networkError));

            $(RefreshHandler.class).refreshEvents(latLng);
            $(RefreshHandler.class).refreshPhysicalGroups(latLng);
        });
        $(MapHandler.class).attach((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        $(MyBubbleHandler.class).start();
        $(ReplyLayoutHandler.class).attach(findViewById(R.id.replyLayout));
        $(StatusLayoutHandler.class).attach(findViewById(R.id.myStatusLayout));
        $(MyGroupsLayoutHandler.class).attach(findViewById(R.id.myGroupsLayout));

        boolean verifiedNumber = $(PersistenceHandler.class).getIsVerified();
        $(MyGroupsLayoutHandler.class).showVerifyMyNumber(!verifiedNumber);
        if (!verifiedNumber) {
            $(DisposableHandler.class).add($(ApiHandler.class).isVerified().subscribe(verified -> {
                $(PersistenceHandler.class).setIsVerified(verified);
                $(MyGroupsLayoutHandler.class).showVerifyMyNumber(!verified);
            }, this::networkError));
        }

        if (getIntent() != null) {
            $(IntentHandler.class).onNewIntent(getIntent());
        }

        String deviceToken = FirebaseInstanceId.getInstance().getToken();

        if (deviceToken != null) {
            $(AccountHandler.class).updateDeviceToken(deviceToken);
        }

        if ($(AccountHandler.class).getActive()) {
            $(AccountHandler.class).updateStatus($(AccountHandler.class).getStatus());
        }

        $(SuggestionHandler.class).attach(findViewById(R.id.actionButton));

        $(MyGroupsLayoutHandler.class).showHelpButton(!$(PersistenceHandler.class).getIsHelpHidden());

        $(EventBubbleHandler.class).attach();
        $(PhysicalGroupBubbleHandler.class).attach();

        findViewById(R.id.showPublicGroups).setOnClickListener(view -> $(SearchActivityHandler.class).show(view));
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean locationPermissionGranted = $(PermissionHandler.class).has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (locationPermissionGranted) {
            $(LocationHandler.class).getCurrentLocation(location -> $(AccountHandler.class).updateGeo(new LatLng(location.getLatitude(), location.getLongitude())));
        }

        boolean locationPermissionDenied = $(PermissionHandler.class).denied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        $(MyGroupsLayoutHandler.class).showAllowLocationPermissionsInSettings(locationPermissionDenied);

        if (locationPermissionGranted && locationPermissionWasDenied) {
            $(MapHandler.class).updateMyLocationEnabled();
            $(MapHandler.class).locateMe();
        }

        locationPermissionWasDenied = locationPermissionDenied;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        $(IntentHandler.class).onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if ($(ReplyLayoutHandler.class).isVisible()) {
            $(ReplyLayoutHandler.class).showReplyLayout(false);
            return;
        }

        super.onBackPressed();
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
        Toast.makeText(this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}
