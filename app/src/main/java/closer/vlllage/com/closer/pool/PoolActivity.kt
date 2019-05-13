package closer.vlllage.com.closer.pool

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

import closer.vlllage.com.closer.App
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.CameraHandler
import closer.vlllage.com.closer.handler.media.MediaHandler

abstract class PoolActivity : androidx.fragment.app.FragmentActivity() {
    val pool = Pool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        `$`(ApplicationHandler::class.java).app = application as App
        `$`(ActivityHandler::class.java).activity = this
    }

    override fun onDestroy() {
        pool.end()
        super.onDestroy()
    }

    protected fun <T : PoolMember> `$`(member: Class<T>): T {
        return pool.`$`(member)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        `$`(PermissionHandler::class.java).onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        `$`(CameraHandler::class.java).onActivityResult(requestCode, resultCode, data)
        `$`(MediaHandler::class.java).onActivityResult(requestCode, resultCode, data)
    }
}
