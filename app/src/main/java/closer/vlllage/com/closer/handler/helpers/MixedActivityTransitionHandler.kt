package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.graphics.Rect
import android.view.View
import closer.vlllage.com.closer.MixedActivity
import com.queatz.on.On

class MixedActivityTransitionHandler(private val on: On) {
    fun showStory(view: View?, storyId: String) {
        val intent = Intent(on<ApplicationHandler>().app, MixedActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(MixedActivity.EXTRA_STORY_ID, storyId)

        if (view != null) {
            val bounds = Rect()
            view.getGlobalVisibleRect(bounds)

            intent.sourceBounds = bounds
        }

        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}