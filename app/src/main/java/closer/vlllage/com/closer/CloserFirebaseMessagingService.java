package closer.vlllage.com.closer;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import closer.vlllage.com.closer.handler.NotificationHandler;
import closer.vlllage.com.closer.util.LatLngStr;

public class CloserFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        App app = (App) getApplication();

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            LatLng latLng = data.containsKey("latLng") ? LatLngStr.to(data.get("latLng")) : null;
            app.getPool().$(NotificationHandler.class).showNotification(
                    data.get("phone"),
                    latLng,
                    data.containsKey("name") ? data.get("name") : "",
                    data.get("message"));
        }
    }
}
