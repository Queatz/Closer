package closer.vlllage.com.closer.handler.helpers


import android.os.Handler
import android.os.Looper

import com.queatz.on.On
import com.queatz.on.OnLifecycle
import io.reactivex.disposables.Disposable

class TimerHandler constructor(private val on: On) : OnLifecycle {

    private lateinit var handler: Handler

    override fun on() {
        handler = Handler(Looper.getMainLooper())
    }

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    fun postDisposable(runnable: Runnable, millis: Long) {
        on<DisposableHandler>().add(post(runnable, millis))
    }

    private fun post(runnable: Runnable, millis: Long): Disposable {
        val disposableRunnable = DisposableRunnable(runnable)
        handler.postDelayed(disposableRunnable, millis)
        return disposableRunnable
    }

    inner class DisposableRunnable constructor(private val runnable: Runnable) : Disposable, Runnable {
        private var disposed: Boolean = false

        override fun dispose() {
            handler.removeCallbacks(runnable)
            disposed = true
        }

        override fun isDisposed() = disposed

        override fun run() {
            disposed = true
            runnable.run()
        }
    }
}
