package closer.vlllage.com.closer.handler.helpers;


import android.os.Handler;
import android.os.Looper;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.disposables.Disposable;

public class TimerHandler extends PoolMember {

    private Handler handler;

    @Override
    protected void onPoolInit() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void post(Runnable runnable) {
        handler.post(runnable);
    }

    public void postDisposable(Runnable runnable, long millis) {
        $(DisposableHandler.class).add(post(runnable, millis));
    }

    private Disposable post(Runnable runnable, long millis) {
        DisposableRunnable disposableRunnable = new DisposableRunnable(runnable);
        handler.postDelayed(disposableRunnable, millis);
        return disposableRunnable;
    }

    public class DisposableRunnable implements Disposable, Runnable {

        private final Runnable runnable;
        private boolean disposed;

        private DisposableRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void dispose() {
            handler.removeCallbacks(runnable);
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void run() {
            disposed = true;
            runnable.run();
        }
    }
}
