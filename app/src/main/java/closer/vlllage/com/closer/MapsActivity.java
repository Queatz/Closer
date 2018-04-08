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

import closer.vlllage.com.closer.api.models.SuggestionResult;
import closer.vlllage.com.closer.handler.AccountHandler;
import closer.vlllage.com.closer.handler.ApiHandler;
import closer.vlllage.com.closer.handler.BubbleHandler;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.IntentHandler;
import closer.vlllage.com.closer.handler.LocationHandler;
import closer.vlllage.com.closer.handler.MapHandler;
import closer.vlllage.com.closer.handler.MyBubbleHandler;
import closer.vlllage.com.closer.handler.MyGroupsLayoutHandler;
import closer.vlllage.com.closer.handler.PermissionHandler;
import closer.vlllage.com.closer.handler.PersistenceHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.ReplyLayoutHandler;
import closer.vlllage.com.closer.handler.SetNameHandler;
import closer.vlllage.com.closer.handler.StatusLayoutHandler;
import closer.vlllage.com.closer.handler.SuggestionHandler;
import closer.vlllage.com.closer.handler.SyncHandler;
import closer.vlllage.com.closer.handler.TimerHandler;
import closer.vlllage.com.closer.handler.bubble.BubbleType;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.bubble.MapBubbleMenuView;
import closer.vlllage.com.closer.pool.PoolActivity;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;

public class MapsActivity extends PoolActivity {

    public static final String EXTRA_LAT_LNG = "latLng";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());
        $(TimerHandler.class).postDisposable(() -> $(SyncHandler.class).syncAll(), 1325);
        $(TimerHandler.class).postDisposable(() -> $(RefreshHandler.class).refreshAll(), 1625);

        $(MapHandler.class).setOnMapReadyListener(map -> $(BubbleHandler.class).attach(map, findViewById(R.id.bubbleMapLayer), mapBubble -> {
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
            $(SuggestionHandler.class).clearSuggestions();

            $(StoreHandler.class).getStore().box(Group.class).query().build().subscribe().single().on(AndroidScheduler.mainThread()).observer(groups -> {
                List<String> groupNames = new ArrayList<>();
                for(Group group : groups) {
                    groupNames.add(group.getName());
                }

                MapBubble menuBubble = new MapBubble(mapBubble.getLatLng(), BubbleType.MENU);
                menuBubble.setPinned(true);
                menuBubble.setOnTop(true);
                $(TimerHandler.class).postDisposable(() -> {
                    $(BubbleHandler.class).add(menuBubble);
                    $(MapBubbleMenuView.class).setMenuTitle(menuBubble, getString(R.string.share_with));
                    $(MapBubbleMenuView.class).getMenuAdapter(menuBubble).setMenuItems(groupNames);
                    menuBubble.setOnItemClickListener(position -> {
                        $(GroupActivityTransitionHandler.class).showGroupMessages(menuBubble.getView(), groups.get(position).getId());
                    });
                }, 225);
            });
        }));
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
                        break;
                    case 1:
                        $(SuggestionHandler.class).createNewSuggestion(menuBubble.getLatLng());
                        break;
                }
            });
            $(BubbleHandler.class).add(menuBubble);
            $(MapBubbleMenuView.class)
                    .getMenuAdapter(menuBubble)
                    .setMenuItems(getString(R.string.share_this_location), getString(R.string.add_suggestion_here));
        });
        $(MapHandler.class).setOnMapIdleListener(latLng -> {
            $(DisposableHandler.class).add($(ApiHandler.class).getPhonesNear(latLng).map(MapBubble::from).subscribe(mapBubbles ->
                            $(BubbleHandler.class).replace(mapBubbles), this::networkError));

            $(DisposableHandler.class).add($(ApiHandler.class).getSuggestionsNear(latLng).map(SuggestionResult::from).subscribe(suggestions ->
                            $(SuggestionHandler.class).loadAll(suggestions), this::networkError));
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
            }));
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        if ($(PermissionHandler.class).has(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            $(LocationHandler.class).getCurrentLocation(location -> $(AccountHandler.class).updateGeo(new LatLng(location.getLatitude(), location.getLongitude())));
        }
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

    private void networkError(Throwable throwable) {
        throwable.printStackTrace();
        Toast.makeText(this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}
