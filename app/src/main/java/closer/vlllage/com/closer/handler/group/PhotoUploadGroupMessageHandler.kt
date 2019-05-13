package closer.vlllage.com.closer.handler.group

import android.net.Uri
import closer.vlllage.com.closer.api.PhotoUploadBackend
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.pool.PoolMember
import java.io.IOException

class PhotoUploadGroupMessageHandler : PoolMember() {
    fun upload(photoUri: Uri, onPhotoUploadedListener: (photoId: String) -> Unit) {
        try {
            `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).uploadPhoto(
                    `$`(ActivityHandler::class.java).activity!!.contentResolver.openInputStream(photoUri)!!)
                    .subscribe({ onPhotoUploadedListener.invoke(it) }, { `$`(DefaultAlerts::class.java).thatDidntWork() }))
        } catch (e: IOException) {
            e.printStackTrace()
            `$`(DefaultAlerts::class.java).thatDidntWork()
        }

    }

    fun getPhotoPathFromId(photoId: String): String {
        return PhotoUploadBackend.BASE_URL + photoId
    }
}
