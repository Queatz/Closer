package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class DisposableHandler extends PoolMember {

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onPoolEnd() {
        disposables.dispose();
    }

    public void add(Disposable disposable) {
        if (disposable.isDisposed()) {
            return;
        }

        disposables.add(disposable);
    }

    public void dispose(Disposable disposable) {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }

        disposables.remove(disposable);
    }
}
