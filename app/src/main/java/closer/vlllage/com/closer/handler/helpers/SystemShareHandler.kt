package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.pool.PoolMember
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SystemShareHandler : PoolMember() {

    fun share(bitmap: Bitmap?) {
        if (bitmap == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val file: File?
        try {
            file = `$`(MediaHandler::class.java).createTemporaryFile("closer-photo", ".jpg")

            if (file == null) {
                `$`(DefaultAlerts::class.java).thatDidntWork()
                return
            }

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 91, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val contentUri = FileProvider.getUriForFile(`$`(ActivityHandler::class.java).activity!!, MediaHandler.AUTHORITY, file)

        if (contentUri == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return
        }

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.setDataAndType(contentUri, `$`(ApplicationHandler::class.java).app!!.contentResolver.getType(contentUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.type = "image/jpg"
        `$`(ActivityHandler::class.java).activity!!.startActivity(Intent.createChooser(shareIntent, `$`(ResourcesHandler::class.java).resources.getString(R.string.share_to)))
    }
}
