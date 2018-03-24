package closer.vlllage.com.closer.handler;


import android.os.Handler;
import android.os.Looper;

import closer.vlllage.com.closer.pool.PoolMember;

public class TimerHandler extends PoolMember {
    private Handler handler;

    @Override
    protected void onPoolInit() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void post(Runnable runnable, long millis) {
        handler.postDelayed(runnable, millis);
    }
}
