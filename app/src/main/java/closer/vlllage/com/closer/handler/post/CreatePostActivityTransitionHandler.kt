package closer.vlllage.com.closer.handler.post

import android.content.Intent
import closer.vlllage.com.closer.CreatePostActivity
import closer.vlllage.com.closer.CreatePostActivity.Companion.EXTRA_GROUP_ID
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import com.queatz.on.On

class CreatePostActivityTransitionHandler constructor(private val on: On) {
    fun show(groupId: String) {
        val intent = Intent(on<ApplicationHandler>().app, CreatePostActivity::class.java)
        intent.action = Intent.ACTION_VIEW

        intent.putExtra(EXTRA_GROUP_ID, groupId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
