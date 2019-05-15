package closer.vlllage.com.closer.handler.group

import android.net.Uri
import closer.vlllage.com.closer.api.PhotoUploadBackend
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On
import java.io.IOException

class PhotoUploadGroupMessageHandler constructor(private val on: On) {
    fun upload(photoUri: Uri, onPhotoUploadedListener: (photoId: String) -> Unit) {
        try {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().uploadPhoto(
                    on<ActivityHandler>().activity!!.contentResolver.openInputStream(photoUri)!!)
                    .subscribe({ onPhotoUploadedListener.invoke(it) }, { on<DefaultAlerts>().thatDidntWork() }))
        } catch (e: IOException) {
            e.printStackTrace()
            on<DefaultAlerts>().thatDidntWork()
        }

    }

    fun getPhotoPathFromId(photoId: String): String {
        return PhotoUploadBackend.BASE_URL + photoId
    }
}
