package closer.vlllage.com.closer;

import com.google.firebase.FirebaseApp;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolApplication;

public class App extends PoolApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        $(ApplicationHandler.class).setApp(this);
        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());
        FirebaseApp.initializeApp(this);
    }
}
