package closer.vlllage.com.closer.pool

import android.app.Fragment
import android.os.Bundle

import closer.vlllage.com.closer.App
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler

open class PoolFragment : Fragment() {
    private val pool = Pool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        `$`(ApplicationHandler::class.java).app = activity.application as App
        `$`(ActivityHandler::class.java).activity = activity
    }

    override fun onDestroy() {
        pool.end()
        super.onDestroy()
    }

    protected fun <T : PoolMember> `$`(member: Class<T>): T {
        return pool.`$`(member)
    }
}
