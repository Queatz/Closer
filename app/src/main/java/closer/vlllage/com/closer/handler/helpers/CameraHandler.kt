package closer.vlllage.com.closer.handler.helpers

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import com.queatz.on.On
import java.io.File

class CameraHandler constructor(private val on: On) {

    companion object {
        private const val REQUEST_CODE_CAMERA = 1044
        private var photoUri: Uri? = null
        private var onPhotoCapturedListener: ((photoUri: Uri?) -> Unit)? = null
    }

    fun showCamera(onPhotoCapturedListener: (photoUri: Uri?) -> Unit) {
        on<PermissionHandler>().check(Manifest.permission.CAMERA).`when` { granted ->
            if (granted) {
                CameraHandler.onPhotoCapturedListener = onPhotoCapturedListener

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                val photo: File?

                try {
                    photo = on<MediaHandler>().createTemporaryFile("picture", ".jpg")

                    photo ?: return@`when`

                    photo.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                    on<DefaultAlerts>().thatDidntWork()
                    return@`when`
                }

                photoUri = FileProvider.getUriForFile(on<ActivityHandler>().activity!!, MediaHandler.AUTHORITY, photo)

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.INTERNAL_CONTENT_URI.path)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                on<ActivityHandler>().activity!!.startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA)
            }
        }
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
}
