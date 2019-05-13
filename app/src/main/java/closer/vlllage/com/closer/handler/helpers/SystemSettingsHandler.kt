package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.net.Uri
import android.provider.Settings

import closer.vlllage.com.closer.pool.PoolMember

class SystemSettingsHandler : PoolMember() {
    fun showSystemSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", `$`(ApplicationHandler::class.java).app!!.packageName, null)
        intent.data = uri
        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }
}
