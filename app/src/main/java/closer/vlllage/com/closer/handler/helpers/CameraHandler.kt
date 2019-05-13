package closer.vlllage.com.closer.handler.helpers

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.pool.PoolMember
import java.io.File

class CameraHandler : PoolMember() {

    private var photoUri: Uri? = null

    private var onPhotoCapturedListener: ((photoUri: Uri?) -> Unit)? = null

    fun showCamera(onPhotoCapturedListener: (photoUri: Uri?) -> Unit) {
        this.onPhotoCapturedListener = onPhotoCapturedListener

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photo: File?

        try {
            photo = `$`(MediaHandler::class.java).createTemporaryFile("picture", ".jpg")

            if (photo == null) {
                return
            }

            photo.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        photoUri = FileProvider.getUriForFile(`$`(ActivityHandler::class.java).activity!!, MediaHandler.AUTHORITY, photo)

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.INTERNAL_CONTENT_URI.path)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        `$`(ActivityHandler::class.java).activity!!.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_CAMERA) {
            return
        }

        if (resultCode != RESULT_OK) {
            return
        }

            onPhotoCapturedListener?.invoke(photoUri)
    }

    companion object {

        private const val REQUEST_CODE_CAMERA = 1044
    }
}
