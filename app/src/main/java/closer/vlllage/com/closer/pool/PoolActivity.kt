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
import com.queatz.on.On

abstract class PoolActivity : FragmentActivity() {

    val on = On()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        on<ApplicationHandler>().app = application as App
        on<ActivityHandler>().activity = this
    }

    override fun onDestroy() {
        on.off()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        on<PermissionHandler>().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        on<CameraHandler>().onActivityResult(requestCode, resultCode, data)
        on<MediaHandler>().onActivityResult(requestCode, resultCode, data)
    }
}
