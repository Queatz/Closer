package closer.vlllage.com.closer.handler.helpers


import android.os.Handler
import android.os.Looper

import closer.vlllage.com.closer.pool.PoolMember
import io.reactivex.disposables.Disposable

class TimerHandler : PoolMember() {

    private var handler: Handler? = null

    override fun onPoolInit() {
        handler = Handler(Looper.getMainLooper())
    }

    fun post(runnable: Runnable) {
        handler!!.post(runnable)
    }

    fun postDisposable(runnable: Runnable, millis: Long) {
        `$`(DisposableHandler::class.java).add(post(runnable, millis))
    }

    private fun post(runnable: Runnable, millis: Long): Disposable {
        val disposableRunnable = DisposableRunnable(runnable)
        handler!!.postDelayed(disposableRunnable, millis)
        return disposableRunnable
    }

    inner class DisposableRunnable constructor(private val runnable: Runnable) : Disposable, Runnable {
        private var disposed: Boolean = false

        override fun dispose() {
            handler!!.removeCallbacks(runnable)
            disposed = true
        }

        override fun isDisposed(): Boolean {
            return disposed
        }

        override fun run() {
            disposed = true
            runnable.run()
        }
    }
}
