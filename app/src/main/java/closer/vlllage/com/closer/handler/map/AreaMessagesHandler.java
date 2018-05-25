package closer.vlllage.com.closer.handler.map;

import java.util.Date;

import closer.vlllage.com.closer.handler.data.LocationHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;

public class AreaMessagesHandler extends PoolMember {
    public void send(String text) {
        $(LocationHandler.class).getCurrentLocation(location -> {
            if ($(PersistenceHandler.class).getPhoneId() == null || location == null) {
                $(DefaultAlerts.class).thatDidntWork();
                return;
            }

            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setText(text);
            groupMessage.setFrom($(PersistenceHandler.class).getPhoneId());
            groupMessage.setTime(new Date());
            groupMessage.setLatitude(location.getLatitude());
            groupMessage.setLongitude(location.getLongitude());
            $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
            $(SyncHandler.class).sync(groupMessage);
        });
    }
}
