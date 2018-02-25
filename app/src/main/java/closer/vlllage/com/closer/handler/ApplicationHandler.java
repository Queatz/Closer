package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.App;
import closer.vlllage.com.closer.pool.PoolMember;

public class ApplicationHandler extends PoolMember {

    private App app;

    public App getApp() {
        return app;
    }

    public ApplicationHandler setApp(App app) {
        this.app = app;
        return this;
    }
}
