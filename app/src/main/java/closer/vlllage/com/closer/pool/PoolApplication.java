package closer.vlllage.com.closer.pool;

import android.app.Application;

public abstract class PoolApplication extends Application {
    private final Pool pool = new Pool();

    @Override
    public void onTerminate() {
        pool.end();
        super.onTerminate();
    }

    protected <T extends PoolMember> T $(Class<T> member) {
        return pool.$(member);
    }

    public Pool getPool() {
        return pool;
    }
}
