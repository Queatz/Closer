package closer.vlllage.com.closer.handler.helpers;

import java.util.HashSet;
import java.util.Set;

import closer.vlllage.com.closer.pool.PoolMember;
import io.objectbox.reactive.DataSubscription;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class DisposableHandler extends PoolMember {

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Set<DataSubscription> dataSubscriptions = new HashSet<>();

    @Override
    protected void onPoolEnd() {
        disposables.dispose();
        for(DataSubscription dataSubscription : dataSubscriptions) {
            if (!dataSubscription.isCanceled()) try {
                dataSubscription.cancel();
            } catch (NullPointerException ignored) {
                // Objectbox NPE
            }
        }
        dataSubscriptions.clear();
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

    public void add(DataSubscription dataSubscription) {
        dataSubscriptions.add(dataSubscription);
    }

    public void dispose(DataSubscription dataSubscription) {
        if (!dataSubscription.isCanceled()) {
            dataSubscription.cancel();
        }

        dataSubscriptions.remove(dataSubscription);
    }
}
