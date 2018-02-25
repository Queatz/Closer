package closer.vlllage.com.closer;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import closer.vlllage.com.closer.handler.AccountHandler;

public class CloserFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        App app = (App) getApplication();
        app.getPool().$(AccountHandler.class).updateDeviceToken(deviceToken);
    }
}
