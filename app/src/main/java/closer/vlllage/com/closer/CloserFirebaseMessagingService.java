package closer.vlllage.com.closer;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import closer.vlllage.com.closer.handler.NotificationHandler;
import closer.vlllage.com.closer.handler.RefreshHandler;
import closer.vlllage.com.closer.handler.TopHandler;
import closer.vlllage.com.closer.util.LatLngStr;

public class CloserFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        App app = (App) getApplication();

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {

            if (data.containsKey("action")) {
                switch (data.get("action")) {
                    case "group.invited":
                        app.$(NotificationHandler.class).showInvitedToGroupNotification(
                                data.get("invitedBy"),
                                data.get("groupName"),
                                data.get("groupId"));

                        app.$(RefreshHandler.class).refreshMyGroups();
                        app.$(RefreshHandler.class).refreshMyMessages();
                        break;
                    case "group.message":
                        if (!app.$(TopHandler.class).isGroupActive(data.get("groupId"))) {
                            app.$(NotificationHandler.class).showGroupMessageNotification(
                                    data.get("text"),
                                    data.get("messageFrom"),
                                    data.get("groupName"),
                                    data.get("groupId"),
                                    data.get("passive"));
                        }

                        app.$(RefreshHandler.class).refreshMyMessages();
                        break;
                    case "refresh":
                        if (data.containsKey("what")) {
                            switch (data.get("what")) {
                                case "groups":
                                    app.$(RefreshHandler.class).refreshMyGroups();
                                    break;
                                case "messages":
                                    app.$(RefreshHandler.class).refreshMyMessages();
                                    break;
                            }
                        }
                        break;
                    case "message":
                        LatLng latLng = data.containsKey("latLng") ? LatLngStr.to(data.get("latLng")) : null;
                        app.$(NotificationHandler.class).showBubbleMessageNotification(
                                data.get("phone"),
                                latLng,
                                data.containsKey("name") ? data.get("name") : "",
                                data.get("message"));
                        break;
                }
            }
        }
    }
}
