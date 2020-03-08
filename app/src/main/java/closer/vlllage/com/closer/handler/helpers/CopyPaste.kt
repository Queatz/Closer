package closer.vlllage.com.closer.handler.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import com.queatz.on.On


class CopyPaste constructor(private val on: On) {

    private val clipboard get() = on<ApplicationHandler>().app.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    fun copy(text: String) {
        val clip = ClipData.newPlainText("Shareable Closer Invite Link", text)
        clipboard.setPrimaryClip(clip)
    }
}
