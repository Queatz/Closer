package closer.vlllage.com.closer.handler.media

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.pool.PoolMember
import java.io.File
import java.io.IOException

class MediaHandler : PoolMember() {

    private var onMediaSelectedListener: ((Uri) -> Unit)? = null

    fun getPhoto(onMediaSelectedListener: (Uri) -> Unit) {
        this.onMediaSelectedListener = onMediaSelectedListener

        val mediaIntent = Intent(Intent.ACTION_GET_CONTENT)
        mediaIntent.type = "image/*"

        `$`(ActivityHandler::class.java).activity!!.startActivityForResult(mediaIntent, REQUEST_CODE_MEDIA)
    }

    @Throws(IOException::class)
    fun createTemporaryFile(part: String, ext: String): File? {
        var tempDir = `$`(ActivityHandler::class.java).activity!!.cacheDir
        tempDir = File(tempDir.absolutePath + "/shared/")
        if (!tempDir.exists()) {
            if (!tempDir.mkdirs()) {
                return null
            }
        }
        return File.createTempFile(part, ext, tempDir)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_MEDIA) {
            return
        }

        if (resultCode != RESULT_OK) {
            return
        }

        data?.data?.let { onMediaSelectedListener?.invoke(it) }
    }

    companion object {
        const val AUTHORITY = "closer.vlllage.com.closer.fileprovider"
        private const val REQUEST_CODE_MEDIA = 1045
    }
}
