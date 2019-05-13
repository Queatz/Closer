package closer.vlllage.com.closer.pool

import android.app.Application

abstract class PoolApplication : Application() {
    private val pool = Pool()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onTerminate() {
        pool.end()
        super.onTerminate()
    }

    fun <T : PoolMember> `$`(member: Class<T>): T {
        return pool.`$`(member)
    }
}
