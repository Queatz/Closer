package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.objectbox.reactive.DataSubscription
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*

class DisposableHandler constructor(private val on: On) : OnLifecycle {

    private val disposableGroup = DisposableGroup()

    override fun off() {
        disposableGroup.dispose()
    }

    fun add(disposable: Disposable) = disposableGroup.add(disposable)
    fun dispose(disposable: Disposable) = disposableGroup.dispose(disposable)
    fun add(dataSubscription: DataSubscription) = disposableGroup.add(dataSubscription)
    fun dispose(dataSubscription: DataSubscription) = disposableGroup.dispose(dataSubscription)

    fun group() = disposableGroup.group()
    fun self() = disposableGroup
}

class DisposableGroup {
    private val disposables = CompositeDisposable()
    private val dataSubscriptions = HashSet<DataSubscription>()
    private val disposableGroups = HashSet<DisposableGroup>()

    fun group() = DisposableGroup().also { add(it) }

    fun add(disposableGroup: DisposableGroup) {
        disposableGroups.add(disposableGroup)
    }

    fun dispose(disposableGroup: DisposableGroup) {
        disposableGroup.dispose()
        disposableGroups.remove(disposableGroup)
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

    fun dispose() {
        disposableGroups.forEach { it.dispose() }
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

    fun clear() {
        disposableGroups.forEach { it.clear() }
        disposables.clear()
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
}