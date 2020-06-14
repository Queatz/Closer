package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.net.Uri
import android.provider.Settings

import com.queatz.on.On

class SystemSettingsHandler constructor(private val on: On) {
    fun showSystemSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", on<ApplicationHandler>().app.packageName, null)
        intent.data = uri
        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
