package closer.vlllage.com.closer;

import com.google.firebase.FirebaseApp;

import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.pool.PoolApplication;

public class App extends PoolApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        $(ApplicationHandler.class).setApp(this);
        FirebaseApp.initializeApp(this);
    }
}
