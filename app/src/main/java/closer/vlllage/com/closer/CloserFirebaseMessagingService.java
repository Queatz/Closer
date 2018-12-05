package closer.vlllage.com.closer;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.data.NotificationHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.TopHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.handler.helpers.LatLngStr;

public class CloserFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        App app = (App) getApplication();

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {

            if (data.containsKey("action")) {
                switch (data.get("action")) {
                    case "event":
                        app.$(NotificationHandler.class).showEventNotification(
                                app.$(JsonHandler.class).from(data.get("event"), Event.class));
                        break;
                    case "group.invited":
                        app.$(NotificationHandler.class).showInvitedToGroupNotification(
                                data.get("invitedBy"),
                                data.get("groupName"),
                                data.get("groupId"));

                        app.$(RefreshHandler.class).refreshMyGroups();
                        app.$(RefreshHandler.class).refreshGroupMessages(data.get("groupId"));
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

                        app.$(RefreshHandler.class).refreshGroupMessages(data.get("groupId"));
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
                        LatLng latLng = data.containsKey("latLng") ? app.$(LatLngStr.class).to(data.get("latLng")) : null;
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
