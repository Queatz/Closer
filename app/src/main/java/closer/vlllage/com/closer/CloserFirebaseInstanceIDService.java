package closer.vlllage.com.closer;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import closer.vlllage.com.closer.handler.data.AccountHandler;

public class CloserFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        ((App) getApplication()).$(AccountHandler.class).updateDeviceToken(deviceToken);
    }
}
