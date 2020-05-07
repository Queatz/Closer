package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.media.MediaHandler
import com.queatz.on.On
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SystemShareHandler constructor(private val on: On) {

    fun share(bitmap: Bitmap?) {
        if (bitmap == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val file: File?
        try {
            file = on<MediaHandler>().createTemporaryFile("closer-photo", ".jpg")

            if (file == null) {
                on<DefaultAlerts>().thatDidntWork()
                return
            }

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 91, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val contentUri = FileProvider.getUriForFile(on<ActivityHandler>().activity!!, MediaHandler.AUTHORITY, file)

        if (contentUri == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(contentUri, on<ApplicationHandler>().app!!.contentResolver.getType(contentUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.type = "image/jpg"
        on<ActivityHandler>().activity!!.startActivity(Intent.createChooser(shareIntent, on<ResourcesHandler>().resources.getString(R.string.share_with)))
    }
}
