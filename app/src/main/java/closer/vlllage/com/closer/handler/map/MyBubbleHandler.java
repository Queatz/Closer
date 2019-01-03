package closer.vlllage.com.closer.handler.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.handler.bubble.BubbleHandler;
import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.TimeStr;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;

public class MyBubbleHandler extends PoolMember {

    private MapBubble myBubble;

    public void updateFrom(AccountHandler.AccountChange accountChange) {
        switch (accountChange.prop) {
            case AccountHandler.ACCOUNT_FIELD_GEO:
                updateLocation((LatLng) accountChange.value);
                break;
            case AccountHandler.ACCOUNT_FIELD_ACTIVE:
                Location location = $(LocationHandler.class).getLastKnownLocation();
                if (location != null) {
                    updateLocation(new LatLng(location.getLatitude(), location.getLatitude()));
                }
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
            Phone phone = $(StoreHandler.class).getStore().box(Phone.class).query().equal(Phone_.id, $(PersistenceHandler.class).getPhoneId()).build().findFirst();
            myBubble = new MapBubble(latLng, $(AccountHandler.class).getName(), $(AccountHandler.class).getStatus());
            myBubble.setPinned(true);

            if (phone != null) {
                myBubble.setTag(phone);
                myBubble.setAction($(TimeStr.class).pretty(phone.getUpdated()));
            }
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
