package closer.vlllage.com.closer.handler.map;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class MyBubbleHandler extends PoolMember {

    private MapBubble myBubble;

    public void updateFrom(AccountHandler.AccountChange accountChange) {
        switch (accountChange.prop) {
            case AccountHandler.ACCOUNT_FIELD_GEO:
                updateLocation((LatLng) accountChange.value);
                break;
            case AccountHandler.ACCOUNT_FIELD_ACTIVE:
                updateActive((boolean) accountChange.value);
                break;
            default:
                update();
        }
    }

    private void updateActive(boolean active) {
        if (myBubble == null) {
            return;
        }

        if (active) {
            $(BubbleHandler.class).add(myBubble);
        } else {
            $(BubbleHandler.class).remove(myBubble);
        }
    }

    private void updateLocation(LatLng latLng) {
        if (myBubble == null) {
            myBubble = new MapBubble(latLng, $(AccountHandler.class).getName(), $(AccountHandler.class).getStatus());
            myBubble.setPinned(true);
            myBubble.setAction($(ResourcesHandler.class).getResources().getString(R.string.update_name));
            updateActive($(AccountHandler.class).getActive());
        } else {
            $(BubbleHandler.class).move(myBubble, latLng);
        }
    }

    private void update() {
        if (myBubble == null) {
            return;
        }

        myBubble.setName($(AccountHandler.class).getName());
        myBubble.setStatus($(AccountHandler.class).getStatus());
        $(BubbleHandler.class).updateDetails(myBubble);
    }

    public void start() {
        $(DisposableHandler.class).add($(AccountHandler.class).changes().subscribe(this::updateFrom, error -> $(ConnectionErrorHandler.class).notifyConnectionError()));
    }

    public boolean isMyBubble(MapBubble mapBubble) {
        return myBubble == mapBubble;
    }

    public MapBubble getMyBubble() {
        return myBubble;
    }
}
