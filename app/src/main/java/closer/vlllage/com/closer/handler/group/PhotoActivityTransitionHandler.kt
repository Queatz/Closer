package closer.vlllage.com.closer.handler.group

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.PhotoActivity
import closer.vlllage.com.closer.PhotoActivity.Companion.EXTRA_PHOTO
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class PhotoActivityTransitionHandler constructor(private val on: On) {
    fun show(view: View?, photo: String) {
        val intent = Intent(on<ApplicationHandler>().app, PhotoActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO, photo)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
