package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.objectbox.reactive.DataSubscription
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

class DisposableHandler constructor(private val on: On) : OnLifecycle {

    private val disposables = CompositeDisposable()
    private val dataSubscriptions = HashSet<DataSubscription>()

    override fun off() {
        disposables.dispose()
        for (dataSubscription in dataSubscriptions) {
            if (!dataSubscription.isCanceled)
                try {
                    dataSubscription.cancel()
                } catch (ignored: NullPointerException) {
                    // Objectbox NPE
                }

        }
        dataSubscriptions.clear()
    }

    fun add(disposable: Disposable) {
        if (disposable.isDisposed) {
            return
        }

        disposables.add(disposable)
    }

    fun dispose(disposable: Disposable) {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }

        disposables.remove(disposable)
    }

    fun add(dataSubscription: DataSubscription) {
        dataSubscriptions.add(dataSubscription)
    }

    fun dispose(dataSubscription: DataSubscription) {
        if (!dataSubscription.isCanceled) {
            dataSubscription.cancel()
        }

        dataSubscriptions.remove(dataSubscription)
    }
}