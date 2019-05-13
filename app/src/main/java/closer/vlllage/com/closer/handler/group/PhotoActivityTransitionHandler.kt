package closer.vlllage.com.closer.handler.group

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.PhotoActivity
import closer.vlllage.com.closer.PhotoActivity.Companion.EXTRA_PHOTO
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolMember

class PhotoActivityTransitionHandler : PoolMember() {
    fun show(view: View?, photo: String) {
        val intent = Intent(`$`(ApplicationHandler::class.java).app, PhotoActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_PHOTO, photo)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        `$`(ActivityHandler::class.java).activity!!.startActivity(intent)
    }
}
